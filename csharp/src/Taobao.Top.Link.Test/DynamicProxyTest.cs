using System;
using System.Collections.Generic;
using System.Runtime.Remoting.Messaging;
using System.Runtime.Remoting.Proxies;
using System.Text;
using NUnit.Framework;
using Taobao.Top.Link.Remoting;

namespace Taobao.Top.Link.Test
{
    [TestFixture]
    public class DynamicProxyTest
    {
        [TestCase]
        public void ProxyTest()
        {
            var testService = new TestProxy(typeof(TestService)).GetTransparentProxy() as TestService;
            Assert.NotNull(testService);
            Assert.AreEqual("hi", testService.Echo("hi"));
        }

        public class TestProxy : RealProxy
        {
            public TestProxy(Type classToProxy) : base(classToProxy) { }
            public override IMessage Invoke(IMessage msg)
            {
                var call = msg as IMethodCallMessage;
                return new MethodResponse(new Header[] { new Header("__Return", call.Args[0]) }, call);
            }
        }
        public interface TestService
        {
            string Echo(string input);
        }
    }
}