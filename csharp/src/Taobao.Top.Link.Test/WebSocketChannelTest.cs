using System;
using System.Collections.Generic;
using System.Text;
using System.Threading;
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
            Assert.True(client.IsConnected);
        }

        [TestCase]
        [ExpectedException(typeof(LinkException))]
        public void ConnectErrorTest()
        {
            WebSocketClient.Connect(new Uri("ws://localhost:1234"), 1000);
        }

        [TestCase]
        public void SendTest()
        {
            var str = "hi中文";
            var handle = new EventWaitHandle(false, EventResetMode.AutoReset);

            IClientChannel channel = WebSocketClient.Connect(URI, 1000);
            channel.OnMessage = new EventHandler<ChannelContext>((o, ctx) =>
            {
                Assert.AreEqual(str, Encoding.UTF8.GetString((byte[])ctx.Message));
                handle.Set();
            });

            byte[] data = Encoding.UTF8.GetBytes(str);
            channel.Send(data);
            Assert.True(handle.WaitOne(500));
        }

        [TestCase]
        public void UnexpectedErrorInHandlerTest()
        {
            IClientChannel channel = WebSocketClient.Connect(URI, 1000);
            channel.OnMessage = new EventHandler<ChannelContext>((o, ctx) =>
            {
                //error in eventhandler should not course channel crash
                throw new Exception();
            });
            channel.Send(new byte[0]);
            Thread.Sleep(500);
        }

        [TestCase]
        public void SharedSelectorTest()
        {
            var selector = new ClientChannelSharedSelector();
            Assert.NotNull(selector.GetChannel(URI));
            Assert.AreSame(selector.GetChannel(URI), selector.GetChannel(URI));
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
                Console.WriteLine("received: " + e.Data);
                this.Send(e.RawData);
            }
        }
    }
}