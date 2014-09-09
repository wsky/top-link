using System;
using System.Collections.Generic;
using System.Runtime.Remoting.Channels;
using System.Runtime.Remoting.Channels.Tcp;
using System.Text;
using NUnit.Framework;
using Top.Link.Channel;
using Top.Link.Endpoints;
using Top.Link.Remoting;
using Top.Link.Remoting.Serialization.Json;
using Top.Link.Util;

namespace Top.Link.Test
{
    /// <summary>interoper with java server, that args[0] is return
    /// </summary>
    [TestFixture]
    public class InteropeTest
    {
        private static Uri URI = new Uri("ws://localhost:9000/");

        public void CallJavaServerTest()
        {
            DynamicProxy proxy = this.CreateProxy(typeof(TestService));
            proxy.SerializationFormat = "json";
            var testService = proxy.GetTransparentProxy() as TestService;

            Assert.AreEqual("hi", testService.Echo("hi"));
            Assert.AreEqual(1, testService.Echo(1));
            var date = DateTime.Now;
            Assert.AreEqual(date, testService.Echo(date));
            Assert.AreEqual("hi", testService.Echo(new string[] { "hi" })[0]);

            var dict1 = new Dictionary<string, string>();
            dict1.Add("k", "k");
            var dict2 = testService.Echo(dict1);
            Assert.AreEqual(dict1["k"], dict2["k"]);
        }

        /// <summary>test .net remoting directly call to top-link
        /// </summary>
        public void BuildInRmotingWithFormatterSinkTest()
        {
            ChannelServices.RegisterChannel(new TcpClientChannel("json", new JsonClientFormatterSinkProvider()), false);

            var testService = System.Runtime.Remoting.RemotingServices.Connect(typeof(TestService)
                , "tcp://localhost:8000/") as TestService;

            Assert.AreEqual("hi", testService.Echo("hi"));
            Assert.AreEqual(1, testService.Echo(1));
            var date = DateTime.Now;
            Assert.AreEqual(date, testService.Echo(date));
            Assert.AreEqual("hi", testService.Echo(new string[] { "hi" })[0]);
        }

        /// <summary>test .net remoting directly call
        /// </summary>
        [TestCase]
        public void BuildInRemotingTest()
        {
            //server
            ChannelServices.RegisterChannel(new TcpServerChannel("server", 8001, new JsonServerFormatterSinkProvider()), false);
            System.Runtime.Remoting.RemotingServices.Marshal(new TestClass(), "remote");
            //client
            ChannelServices.RegisterChannel(new TcpClientChannel("client", new JsonClientFormatterSinkProvider()), false);
            var testService = System.Runtime.Remoting.RemotingServices.Connect(typeof(TestService)
                , "tcp://localhost:8001/remote") as TestService;
            Assert.AreEqual("hi", testService.Echo("hi"));
            Assert.AreEqual(1, testService.Echo(1));
        }

        public void EndpointTest()
        {
            var e = new Endpoint(new DefaultLoggerFactory(true, true, true, true, true), new SimpleIdentity("e1"));
            EndpointProxy proxy = e.GetEndpoint(new SimpleIdentity("e2"), "ws://localhost:9090/");
            var msg = new Dictionary<string, object>();
            msg.Add("byte", (byte)1);
            msg.Add("string", "string");
            msg.Add("int16", (short)123);
            msg.Add("int32", 156);
            msg.Add("int64", 178L);
            msg.Add("date", DateTime.Now);
            var content = "hi中文";
            msg.Add("byte[]", GZIPHelper.Zip(Encoding.UTF8.GetBytes(content)));
            var msg2 = proxy.SendAndWait(msg);
            foreach (var i in msg)
            {
                if (msg2[i.Key] is byte[])
                {
                    foreach (var b in msg2[i.Key] as byte[])
                        Console.Write(b + ",");
                    Console.WriteLine();
                    var str = Encoding.UTF8.GetString(GZIPHelper.Unzip(msg2[i.Key] as byte[]));
                    Assert.AreEqual(content, str);
                    Console.WriteLine(i.Key + "=" + str);
                    continue;
                }
                Assert.AreEqual(msg[i.Key], msg2[i.Key]);
                Assert.AreEqual(msg[i.Key].GetType(), msg2[i.Key].GetType());
                Console.WriteLine(i.Key + "=" + msg2[i.Key]);
            }
        }

        private DynamicProxy CreateProxy(Type type)
        {
            return new DynamicProxy(type, URI
                , new RemotingHandler(DefaultLoggerFactory.Default
                , new ClientChannelSharedSelector()
                , new CrossLanguageSerializationFactory()));
        }

        public interface TestService
        {
            string Echo(string input);
            int Echo(int input);
            DateTime Echo(DateTime input);
            string[] Echo(string[] input);
            IDictionary<string, string> Echo(IDictionary<string, string> input);
        }
        public class TestClass : MarshalByRefObject, TestService
        {
            public string Echo(string input)
            {
                return input;
            }
            public int Echo(int input)
            {
                return input;
            }
            public DateTime Echo(DateTime input)
            {
                return input;
            }
            public string[] Echo(string[] input)
            {
                return input;
            }
            public IDictionary<string, string> Echo(IDictionary<string, string> input)
            {
                return input;
            }
        }
    }
}