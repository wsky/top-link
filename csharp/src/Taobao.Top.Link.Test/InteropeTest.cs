using System;
using System.Collections.Generic;
using System.Runtime.Remoting.Channels;
using System.Runtime.Remoting.Channels.Tcp;
using System.Text;
using NUnit.Framework;
using Taobao.Top.Link.Channel;
using Taobao.Top.Link.Remoting;
using Taobao.Top.Link.Remoting.Serialization.Json;

namespace Taobao.Top.Link.Test
{
    /// <summary>interoper with java server, that args[0] is return
    /// </summary>
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
            //Assert.AreEqual(date, testService.Echo(DateTime.Now));
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
            var provider = new JsonClientFormatterSinkProvider();
            ChannelServices.RegisterChannel(new TcpClientChannel("json", new JsonClientFormatterSinkProvider()), false);

            var testService = System.Runtime.Remoting.RemotingServices.Connect(typeof(TestService)
                , "tcp://localhost:8000/") as TestService;

            Assert.AreEqual("hi", testService.Echo("hi"));
            Assert.AreEqual(1, testService.Echo(1));
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
    }
}