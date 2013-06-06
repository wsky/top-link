using System;
using System.Collections.Generic;
using System.IO;
using System.Text;
using System.Threading;
using RemotingProtocolParser.TCP;
using Taobao.Top.Link.Channel;
using Taobao.Top.Link.Remoting.Protocol;

namespace Taobao.Top.Link.Remoting
{
    /// <summary>deal with protocol/callback
    /// </summary>
    public class RemotingHandler
    {
        private ILog _log;
        private int _flag;
        private IClientChannelSelector _channelSelector;
        private ISerializationFactory _serializationFactory;
        private IDictionary<int, RemotingCallback> _callbacks;

        private EventHandler<ChannelClosedEventArgs> _onClosed;
        private EventHandler<ChannelContext> _onMessage;

        public RemotingHandler(ILoggerFactory loggerFactory
            , IClientChannelSelector channelSelector
            , ISerializationFactory serializationFactory)
        {
            this._log = loggerFactory.Create(this);
            this._channelSelector = channelSelector;
            this._serializationFactory = serializationFactory;
            this._callbacks = new Dictionary<int, RemotingCallback>();
            this.PrepareEventHandler();
        }

        /// <summary>send call request to remote and pending callback
        /// </summary>
        /// <param name="remoteUri"></param>
        /// <param name="operation"></param>
        /// <param name="transportHeaders"></param>
        /// <param name="call"></param>
        /// <param name="callback"></param>
        public void Call(Uri remoteUri
            , ushort operation
            , IDictionary<string, object> transportHeaders
            , MethodCall call
            , RemotingCallback callback)
        {
            var data = this._serializationFactory.Get(callback.SerializationFormat).SerializeMethodCall(call);
            var flag = Interlocked.Increment(ref this._flag);
            callback.Flag = flag;
            transportHeaders.Add(RemotingTransportHeader.Flag, flag);
            transportHeaders.Add(RemotingTransportHeader.Format, callback.SerializationFormat);

            var request = new MemoryStream();
            var handle = new RemotingTcpProtocolHandle(request);
            handle.WritePreamble();
            handle.WriteMajorVersion();
            handle.WriteMinorVersion();
            handle.WriteOperation(TcpOperations.Request);
            handle.WriteContentDelimiter(TcpContentDelimiter.ContentLength);
            handle.WriteContentLength(data.Length);
            handle.WriteTransportHeaders(transportHeaders);
            handle.WriteContent(data);

            this.GetChannel(remoteUri).Send(request.ToArray());
            this._callbacks.Add(flag, callback);

            if (this._log.IsDebugEnabled)
                this._log.DebugFormat("pending methodCall#{0}|{1}", flag, remoteUri);
        }
        /// <summary>cancel callback
        /// </summary>
        /// <param name="callback"></param>
        public void Cancel(RemotingCallback callback)
        {
            this._callbacks.Remove(callback.Flag);
        }

        private IClientChannel GetChannel(Uri uri)
        {
            IClientChannel channel = this._channelSelector.GetChannel(uri);
            if (channel.OnClosed == null)
                channel.OnClosed = this._onClosed;
            if (channel.OnMessage == null)
                channel.OnMessage = this._onMessage;
            return channel;
        }
        private void PrepareEventHandler()
        {
            this._onMessage = new EventHandler<ChannelContext>(this.onMessage);
            this._onClosed = new EventHandler<ChannelClosedEventArgs>(onClosed);
        }
        private void onMessage(object sender, ChannelContext ctx)
        {
            var protocol = new RemotingTcpProtocolHandle(new MemoryStream((byte[])ctx.Message));
            protocol.ReadPreamble();
            protocol.ReadMajorVersion();
            protocol.ReadMinorVersion();

            ushort operation = protocol.ReadOperation();
            if (operation != TcpOperations.Reply)
                return;

            protocol.ReadContentDelimiter();
            protocol.ReadContentLength();

            IDictionary<String, object> transportHeaders = protocol.ReadTransportHeaders();
            if (!transportHeaders.ContainsKey(RemotingTransportHeader.Flag))
                return;

            var flag = (int)transportHeaders[RemotingTransportHeader.Flag];

            if (this._log.IsDebugEnabled)
                this._log.DebugFormat("receive methodReturn of methodCall#{0}", flag);

            if (this._callbacks.ContainsKey(flag))
                return;

            RemotingCallback callback = this._callbacks[flag];
            this.Cancel(callback);

            var statusCode = (int)transportHeaders[TcpTransportHeader.StatusCode];
            var statusPhrase = (string)transportHeaders[TcpTransportHeader.StatusPhrase];
            if (statusCode > 0 || !string.IsNullOrEmpty(statusPhrase))
            {
                callback.OnException(new RemotingException(string.Format(
                    "remote reutrn error#%s: %s"
                    , statusCode
                    , statusPhrase)));
                return;
            }

            MethodReturn methodReturn = null;
            try
            {
                methodReturn = this._serializationFactory
                    .Get(callback.SerializationFormat)
                    .DeserializeMethodReturn(protocol.ReadContent(), callback.ReturnType);
            }
            catch (Exception e)
            {
                callback.OnException(e);
                return;
            }

            try { callback.OnMethodReturn(methodReturn); }
            catch (Exception e) { this._log.Error(e); }
        }
        private void onClosed(object sender, ChannelClosedEventArgs args)
        {
            var error = new RemotingException("channel broken");
            foreach (KeyValuePair<int, RemotingCallback> i in this._callbacks)
            {
                try { i.Value.OnException(error); }
                catch (Exception e) { this._log.Error(e); }
            }
            this._callbacks.Clear();
        }
    }
}