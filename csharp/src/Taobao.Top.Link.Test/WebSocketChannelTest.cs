using System;
using System.Collections.Generic;
using System.Text;
using NUnit.Framework;
using Taobao.Top.Link.Channel;
using Taobao.Top.Link.Channel.WebSocket;
using WebSocketSharp;
using WebSocketSharp.Server;

namespace Taobao.Top.Link.Test
{
    [TestFixture]
    public class WebSocketChannelTest
    {
        private static Uri URI = new Uri("ws://localhost:8889/echo");
        private static WebSocketServer server;

        [TestFixtureSetUp]
        public void SetUp()
        {
            server = new WebSocketServer(URI.Port);
            server.AddService<Echo>(URI.AbsolutePath);
            server.Start();
        }

        [TestFixtureTearDown]
        public void TearDown()
        {
            server.Stop();
        }

        [TestCase]
        public void ConnectTest()
        {
            var client = WebSocketClient.Connect(URI, 1000);
            Assert.NotNull(client);
            Assert.True(client.IsConnected());
        }

        [TestCase]
        public void ConnectTimeoutTest()
        {
            WebSocketClient.Connect(new Uri("ws://localhost:1234"), 1000);
        }

        [TestCase]
        public void SharedSelectorTest()
        {
            var selector = new ClientChannelSharedSelector();
            Assert.NotNull(selector.GetChannel(URI));
            Assert.ReferenceEquals(selector.GetChannel(URI), selector.GetChannel(URI));
        }

        [TestCase]
        public void HeartbeatTest()
        {
            var selector = new ClientChannelSharedSelector();
            selector.GetChannel(URI);
        }

        class Echo : WebSocketService
        {
            protected override void OnMessage(object sender, MessageEventArgs e)
            {
                this.Send(e.Data);
            }
        }
    }
}