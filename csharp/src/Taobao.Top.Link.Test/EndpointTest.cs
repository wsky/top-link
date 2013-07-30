using System;
using System.Collections.Generic;
using System.IO;
using System.Text;
using System.Threading;
using NUnit.Framework;
using Taobao.Top.Link.Endpoints;
using WebSocketSharp;
using WebSocketSharp.Server;

namespace Taobao.Top.Link.Test
{
    [TestFixture]
    public class EndpointTest
    {
        private static Uri URI = new Uri("ws://localhost:8889/endpoint");
        private static WebSocketServer server;

        [TestFixtureSetUp]
        public void TestFixtureSetUp()
        {
            server = new WebSocketServer(URI.Port);
            server.AddService<Server>(URI.AbsolutePath);
            server.Start();
        }

        [TestFixtureTearDown]
        public void TestFixtureTearDown()
        {
            server.Stop();
        }

        [TestCase]
        public void ConnectTest()
        {
            var e = new Endpoint(new SimpleIdentity("e1"));
            Assert.NotNull(e.GetEndpoint(new SimpleIdentity("e2"), URI.ToString()));
            Assert.NotNull(e.GetEndpoint(new SimpleIdentity("e2")));
        }

        [TestCase]
        public void SendTest()
        {
            var e = new Endpoint(new SimpleIdentity("e1"));
            var id = new SimpleIdentity("e2");
            var msg = new Dictionary<string, object>();
            msg.Add("k", "k");
            var handle = new EventWaitHandle(false, EventResetMode.AutoReset);
            e.OnAckMessage += (s, ack) =>
            {
                Assert.AreEqual(msg["k"], ack.Message["k"]);
                handle.Set();
            };
            e.GetEndpoint(id, URI.ToString()).Send(msg);
            Assert.True(handle.WaitOne(100));
        }

        [TestCase]
        public void SendAndWaitTest()
        {
            var e = new Endpoint(new SimpleIdentity("e1"));
            var id = new SimpleIdentity("e2");
            var msg = new Dictionary<string, object>();
            msg.Add("k", "k");
            Assert.AreEqual(msg["k"], e.GetEndpoint(id, URI.ToString()).SendAndWait(msg)["k"]);
        }

        class Server : WebSocketService
        {
            protected override void OnMessage(object sender, MessageEventArgs e)
            {
                Message msg = MessageIO.ReadMessage(new MemoryStream(e.RawData));

                if (msg.MessageType == MessageType.CONNECT)
                {
                    msg.MessageType = MessageType.CONNECTACK;
                    msg.Token = Guid.NewGuid().ToString();
                    Console.WriteLine("accept connect-in");
                }
                else
                    msg.MessageType = MessageType.SENDACK;

                if (msg.Content != null)
                    foreach (var p in msg.Content)
                        Console.WriteLine("{0}={1}", p.Key, p.Value);

                using (var s = new MemoryStream())
                {
                    MessageIO.WriteMessage(s, msg);
                    this.Send(s.ToArray());
                }
            }
        }
    }
}