using System;
using System.Collections.Generic;
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
            Assert.AreEqual(1, testService.Echo(1));
            Assert.AreEqual("hi", testService.Echo(new string[] { "hi" })[0]);

            var dict1 = new Dictionary<string, string>();
            dict1.Add("k", "k");
            var dict2 = testService.Echo(dict1);
            Assert.AreEqual(dict1["k"], dict2["k"]);
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