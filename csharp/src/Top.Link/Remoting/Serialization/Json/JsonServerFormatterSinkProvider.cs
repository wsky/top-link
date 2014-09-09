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
    public class JsonServerFormatterSinkProvider : IServerFormatterSinkProvider
    {
        public IServerChannelSinkProvider Next { get; set; }
        public void GetChannelData(IChannelDataStore channelData) { }

        public IServerChannelSink CreateSink(IChannelReceiver channel)
        {
            if (this.Next != null)
            {
                var nextChannelSink = Next.CreateSink(channel);
                if (nextChannelSink != null)
                    return new JsonServerFormatterSink(nextChannelSink);
            }
            return null;

        }

        public class JsonServerFormatterSink : IServerChannelSink
        {
            private static readonly CrossLanguageJsonSerializer _serializer = new CrossLanguageJsonSerializer();
            private readonly IServerChannelSink _nextChannelSink;
            public IServerChannelSink NextChannelSink { get { return this._nextChannelSink; } }
            public IDictionary Properties { get { return null; } }

            public JsonServerFormatterSink(IServerChannelSink nextChannelSink)
            {
                this._nextChannelSink = nextChannelSink;
            }

            public Stream GetResponseStream(IServerResponseChannelSinkStack sinkStack
                , object state
                , IMessage msg
                , ITransportHeaders headers) { throw new NotImplementedException(); }

            public void AsyncProcessResponse(IServerResponseChannelSinkStack sinkStack
                , object state
                , IMessage msg
                , ITransportHeaders headers
                , Stream stream)
            {
                SerializeResponseMessage(sinkStack, msg, ref headers, ref stream);
                sinkStack.AsyncProcessResponse(msg, headers, stream);
            }
            public ServerProcessing ProcessMessage(IServerChannelSinkStack sinkStack
                , IMessage requestMsg
                , ITransportHeaders requestHeaders
                , Stream requestStream
                , out IMessage responseMsg
                , out ITransportHeaders responseHeaders
                , out Stream responseStream)
            {
                if (requestMsg != null)
                    return this.NextChannelSink.ProcessMessage(sinkStack
                        , requestMsg
                        , requestHeaders
                        , requestStream
                        , out responseMsg
                        , out responseHeaders
                        , out responseStream);

                requestMsg = DeserializeRequestMessage(requestHeaders, requestStream);

                sinkStack.Push(this, null);

                var result = this.NextChannelSink.ProcessMessage(sinkStack
                    , requestMsg
                    , requestHeaders
                    , null
                    , out responseMsg
                    , out responseHeaders
                    , out responseStream);

                switch (result)
                {
                    case ServerProcessing.Complete:
                        sinkStack.Pop(this);
                        SerializeResponseMessage(sinkStack, responseMsg, ref responseHeaders, ref responseStream);
                        break;
                    case ServerProcessing.OneWay:
                        sinkStack.Pop(this);
                        break;
                    case ServerProcessing.Async:
                        sinkStack.Store(this, null);
                        break;
                }

                return result;
            }

            private static IMessage DeserializeRequestMessage(ITransportHeaders requestHeaders, Stream requestStream)
            {
                if (requestHeaders == null)
                    throw new ArgumentNullException("requestHeaders");
                if (requestStream == null)
                    throw new ArgumentNullException("requestStream");

                var requestUri = (string)requestHeaders["__RequestUri"];
                if (requestUri == null)
                    throw new RemotingException("Request URI not specified.");

                var serverType = System.Runtime.Remoting.RemotingServices.GetServerTypeForUri(requestUri);
                if (serverType == null)
                    throw new RemotingException(string.Format(
                        "Request URI '{0}' not published for remoting.", requestUri));

                using (requestStream)
                using (var temp = new MemoryStream())
                {
                    CopyStream(requestStream, temp);
                    byte[] data = temp.ToArray();
                    MethodCall request = _serializer.DeserializeMethodCall(data);
                    var serverMethod = GetServerMethod(serverType, request.MethodName, request.MethodSignature);

                    if (serverMethod == null)
                        throw new RemotingException(string.Format(
                            "Invalid request method \"{0}\" specified.", request.MethodName));

                    var headers = new List<Header>(){
					    new Header("__Uri", requestUri),
					    new Header("__TypeName", serverMethod.DeclaringType.AssemblyQualifiedName),
					    new Header("__MethodName", request.MethodName),
					    new Header("__MethodSignature", request.MethodSignature),
					    new Header("__Args", request.Args)
				    };
                    return new System.Runtime.Remoting.Messaging.MethodCall(headers.ToArray());
                }
            }
            private static void SerializeResponseMessage(IServerResponseChannelSinkStack sinkStack
                , IMessage responseMsg
                , ref ITransportHeaders responseHeaders
                , ref Stream responseStream)
            {
                if (sinkStack == null)
                    throw new ArgumentNullException("sinkStack");
                if (responseMsg == null)
                    throw new ArgumentNullException("responseMsg");

                var methodReturnMessage = responseMsg as IMethodReturnMessage;
                if (methodReturnMessage == null)
                    throw new ArgumentException(string.Format(
                        "Invalid response message type: '{0}'.", responseMsg.GetType()), "responseMsg");

                if (responseHeaders == null)
                    responseHeaders = new TransportHeaders();

                bool shouldRewindStream = false;

                if (responseStream == null)
                {
                    responseStream = sinkStack.GetResponseStream(responseMsg, responseHeaders);

                    if (responseStream == null)
                    {
                        responseStream = new MemoryStream();
                        shouldRewindStream = true;
                    }
                }

                byte[] data = _serializer.SerializeMethodReturn(new MethodReturn()
                {
                    Exception = methodReturnMessage.Exception,
                    ReturnValue = methodReturnMessage.ReturnValue
                });
                responseStream.Write(data, 0, data.Length);

                if (shouldRewindStream)
                {
                    responseStream.Position = 0;
                }
            }
            private static MethodInfo GetServerMethod(Type serverType, string requestMethod, Type[] types)
            {
                return serverType.GetMethod(requestMethod, types);
                //, BindingFlags.Instance | BindingFlags.Public | BindingFlags.FlattenHierarchy);
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