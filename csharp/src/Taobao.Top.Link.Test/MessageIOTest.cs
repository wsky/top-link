using System;
using System.Collections.Generic;
using System.IO;
using System.Text;
using NUnit.Framework;
using Taobao.Top.Link.Endpoints;

namespace Taobao.Top.Link.Test
{
    [TestFixture]
    public class MessageIOTest
    {
        [TestCase]
        public void TypeEquals()
        {
            Assert.AreEqual(typeof(byte), typeof(Byte));
            Assert.AreEqual(typeof(string), typeof(String));
            Assert.AreEqual(typeof(short), typeof(Int16));
            Assert.AreEqual(typeof(int), typeof(Int32));
            Assert.AreEqual(typeof(long), typeof(Int64));
        }

        [TestCase]
        public void ReadWriteTest()
        {
            var s = new MemoryStream();
            var msg = new Message();
            msg.MessageType = MessageType.SEND;
            msg.StatusPhase = "error";
            msg.Content = new Dictionary<string, object>();
            msg.Content.Add("byte", (byte)1);
            msg.Content.Add("string", "string");
            msg.Content.Add("int16", (short)123);
            msg.Content.Add("int32", 156);
            msg.Content.Add("int64", 178L);
            msg.Content.Add("date", DateTime.Now);
            MessageIO.WriteMessage(s, msg);

            s.Position = 0;

            var msg2 = MessageIO.ReadMessage(s);
            Assert.AreEqual(msg.MessageType, msg2.MessageType);
            Assert.AreEqual(msg.StatusPhase, msg2.StatusPhase);
            foreach (var i in msg.Content)
            {
                Assert.AreEqual(msg.Content[i.Key], msg2.Content[i.Key]);
                Assert.AreEqual(msg.Content[i.Key].GetType(), msg2.Content[i.Key].GetType());
                Console.WriteLine(i.Key + "=" + msg2.Content[i.Key]);
            }
        }
    }
}