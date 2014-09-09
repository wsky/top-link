using System;
using System.Collections;
using System.Collections.Generic;
using System.IO;
using System.Reflection;
using System.Runtime.Remoting.Channels;
using System.Runtime.Remoting.Messaging;
using System.Text;

namespace Top.Link.Remoting.Serialization.Json
{
    /// <summary>.net remoting formatterSink that support interope
    /// </summary>
    public class JsonClientFormatterSinkProvider : IClientFormatterSinkProvider
    {
        public IClientChannelSinkProvider Next { get; set; }

        public IClientChannelSink CreateSink(IChannelSender channel, string url, object remoteChannelData)
        {
            if (this.Next != null)
            {
                var nextChannelSink = Next.CreateSink(channel, url, remoteChannelData);
                if (nextChannelSink != null)
                    return new JsonClientFormatterSink(nextChannelSink);
            }
            return null;
        }

        public class JsonClientFormatterSink : IClientFormatterSink
        {
            private static readonly CrossLanguageJsonSerializer _serializer = new CrossLanguageJsonSerializer();
            private readonly IClientChannelSink _nextChannelSink;

            public IClientChannelSink NextChannelSink { get { return this._nextChannelSink; } }
            public IDictionary Properties { get { return null; } }

            public JsonClientFormatterSink(IClientChannelSink nextChannelSink)
            {
                this._nextChannelSink = nextChannelSink;
            }

            #region unnecessary at client
            public IMessageSink NextSink
            {
                get { throw new NotImplementedException(); }
            }
            public void ProcessMessage(IMessage msg, ITransportHeaders requestHeaders, Stream requestStream, out ITransportHeaders responseHeaders, out Stream responseStream)
            {
                throw new NotImplementedException();
            }
            public void AsyncProcessRequest(IClientChannelSinkStack sinkStack, IMessage msg, ITransportHeaders headers, Stream stream)
            {
                throw new NotImplementedException();
            }
            public Stream GetRequestStream(IMessage msg, ITransportHeaders headers)
            {
                throw new NotImplementedException();
            }
            #endregion

            public IMessage SyncProcessMessage(IMessage msg)
            {
                var methodCallMessage = (IMethodCallMessage)msg;
                try
                {
                    ITransportHeaders requestHeaders;
                    Stream requestStream;
                    SerializeRequestMessage(methodCallMessage, out requestHeaders, out requestStream);

                    ITransportHeaders responseHeaders;
                    Stream responseStream;
                    this._nextChannelSink.ProcessMessage(methodCallMessage, requestHeaders, requestStream, out responseHeaders, out responseStream);

                    return DeserializeResponseMessage(methodCallMessage, responseHeaders, responseStream);
                }
                catch (Exception ex)
                {
                    return new ReturnMessage(ex, methodCallMessage);
                }
            }
            public IMessageCtrl AsyncProcessMessage(IMessage msg, IMessageSink replySink)
            {
                var methodCallMessage = (IMethodCallMessage)msg;
                try
                {
                    ITransportHeaders requestHeaders;
                    Stream requestStream;
                    SerializeRequestMessage(methodCallMessage, out requestHeaders, out requestStream);

                    var sinkStack = new ClientChannelSinkStack(replySink);
                    sinkStack.Push(this, methodCallMessage);

                    this._nextChannelSink.AsyncProcessRequest(sinkStack, methodCallMessage, requestHeaders, requestStream);
                }
                catch (Exception ex)
                {
                    var errorMessage = new ReturnMessage(ex, methodCallMessage);
                    if (replySink != null)
                    {
                        replySink.SyncProcessMessage(errorMessage);
                    }
                }

                return null;
            }
            public void AsyncProcessResponse(IClientResponseChannelSinkStack sinkStack
                , object state
                , ITransportHeaders headers
                , Stream stream)
            {
                var methodCallMessage = (IMethodCallMessage)state;
                var responseMessage = DeserializeResponseMessage(methodCallMessage, headers, stream);
                sinkStack.DispatchReplyMessage(responseMessage);
            }

            private void SerializeRequestMessage(IMethodCallMessage methodCallMessage
                , out ITransportHeaders requestHeaders
                , out Stream requestStream)
            {
                requestHeaders = new TransportHeaders();
                requestHeaders["RequestMethod"] = methodCallMessage.MethodName;
                requestHeaders["Format"] = "json";

                var shouldRewindStream = false;

                requestStream = this._nextChannelSink.GetRequestStream(methodCallMessage, requestHeaders);
                if (requestStream == null)
                {
                    requestStream = new MemoryStream();
                    shouldRewindStream = true;
                }

                byte[] data = _serializer.SerializeMethodCall(new MethodCall()
                {
                    Uri = methodCallMessage.Uri,
                    MethodName = methodCallMessage.MethodName,
                    TypeName = methodCallMessage.TypeName,
                    MethodSignature = (Type[])methodCallMessage.MethodSignature,
                    Args = methodCallMessage.Args
                });
                requestStream.Write(data, 0, data.Length);

                if (shouldRewindStream)
                    requestStream.Position = 0;
            }
            private static IMessage DeserializeResponseMessage(IMethodCallMessage methodCallMessage
                , ITransportHeaders responseHeaders
                , Stream responseStream)
            {

                using (responseStream)
                using (var temp = new MemoryStream())
                {
                    CopyStream(responseStream, temp);
                    byte[] data = temp.ToArray();
                    var methodReturn = _serializer.DeserializeMethodReturn(data, (methodCallMessage.MethodBase as MethodInfo).ReturnType);

                    return (methodReturn.Exception == null)
                        ? new ReturnMessage(methodReturn.ReturnValue, null, 0, null, methodCallMessage)
                        : new ReturnMessage(methodReturn.Exception, methodCallMessage);
                }
            }
            //TODO:should be improved
            private static void CopyStream(Stream input, Stream output)
            {
                byte[] b = new byte[1024];
                int r;
                while ((r = input.Read(b, 0, b.Length)) > 0)
                    output.Write(b, 0, r);
            }
        }
    }
}