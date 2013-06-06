using System;
using System.Collections.Generic;
using System.IO;
using System.Text;
using NUnit.Framework;
using RemotingProtocolParser.TCP;
using Taobao.Top.Link.Channel;
using Taobao.Top.Link.Remoting;
using Taobao.Top.Link.Remoting.Protocol;
using WebSocketSharp;
using WebSocketSharp.Server;

namespace Taobao.Top.Link.Test
{
    [TestFixture]
    public class DynamicProxyTest
    {
        private static ISerializationFactory serializationFactory = new TestSerializationFactory();
        private static Uri URI = new Uri("ws://localhost:8889/remoting");
        private static WebSocketServer server;

        [TestFixtureSetUp]
        public void SetUp()
        {
            server = new WebSocketServer(URI.Port);
            server.AddService<Remoting>(URI.AbsolutePath);
            server.Start();
        }

        [TestFixtureTearDown]
        public void TearDown()
        {
            server.Stop();
        }

        [TestCase]
        public void InvokeTest()
        {
            var testService = new DynamicProxy(typeof(TestService), URI
                , new RemotingHandler(DefaultLoggerFactory.Default
                , new ClientChannelSharedSelector()
                , serializationFactory))
                {
                    //ExecutionTimeout = 1000
                }.GetTransparentProxy() as TestService;
            Assert.AreEqual("hi", testService.Echo("hi"));
        }

        /// <summary>run as a remoting server
        /// </summary>
        class Remoting : WebSocketService
        {
            protected override void OnMessage(object sender, MessageEventArgs e)
            {
                var protocol = new RemotingTcpProtocolHandle(new MemoryStream(e.RawData));
                protocol.ReadPreamble();
                protocol.ReadMajorVersion();
                protocol.ReadMinorVersion();
                ushort operation = protocol.ReadOperation();
                protocol.ReadContentDelimiter();
                protocol.ReadContentLength();
                IDictionary<string, Object> transportHeaders = protocol.ReadTransportHeaders();
                var flag = (int)transportHeaders[RemotingTransportHeader.Flag];
                ISerializer serializer = serializationFactory.Get(null);
                transportHeaders.Clear();
                transportHeaders.Add(RemotingTransportHeader.Flag, flag);

                MethodCall methodCall = serializer.DeserializeMethodCall(protocol.ReadContent());
                var methodReturn = new MethodReturn() { ReturnValue = methodCall.Args[0] };

                byte[] data = serializer.SerializeMethodReturn(methodReturn);

                var response = new MemoryStream();
                var handle = new RemotingTcpProtocolHandle(response);
                handle.WritePreamble();
                handle.WriteMajorVersion();
                handle.WriteMinorVersion();
                handle.WriteOperation(TcpOperations.Reply);
                handle.WriteContentDelimiter(TcpContentDelimiter.ContentLength);
                handle.WriteContentLength(data.Length);
                handle.WriteTransportHeaders(transportHeaders);
                handle.WriteContent(data);

                this.Send(response.ToArray());
            }
        }
        public interface TestService
        {
            string Echo(string input);
        }
        public class TestSerializer : ISerializer
        {
            private MethodCall _c;
            private MethodReturn _r;
            public string Name { get { return "test"; } }
            public byte[] SerializeMethodCall(MethodCall methodCall)
            {
                _c = methodCall;
                return new byte[0];
            }
            public MethodReturn DeserializeMethodReturn(byte[] input, Type returnType)
            {
                return _r;
            }
            public byte[] SerializeMethodReturn(MethodReturn methodReturn)
            {
                _r = methodReturn;
                return new byte[0];
            }
            public MethodCall DeserializeMethodCall(byte[] input)
            {
                return _c;
            }
        }
        public class TestSerializationFactory : ISerializationFactory
        {
            private ISerializer s = new TestSerializer();
            public ISerializer Get(object format)
            {
                return s;
            }
        }

        #region .net remoting realproxy test
        [TestCase]
        public void ProxyTest()
        {
            var testService = new TestProxy(typeof(TestService)).GetTransparentProxy() as TestService;
            Assert.NotNull(testService);
            Assert.AreEqual("hi", testService.Echo("hi"));
        }
        public class TestProxy : System.Runtime.Remoting.Proxies.RealProxy
        {
            public TestProxy(Type classToProxy) : base(classToProxy) { }
            public override System.Runtime.Remoting.Messaging.IMessage Invoke(System.Runtime.Remoting.Messaging.IMessage msg)
            {
                var call = msg as System.Runtime.Remoting.Messaging.IMethodCallMessage;
                return new System.Runtime.Remoting.Messaging.MethodResponse(new System.Runtime.Remoting.Messaging.Header[] { new System.Runtime.Remoting.Messaging.Header("__Return", call.Args[0]) }, call);
            }
        }
        #endregion
    }
}