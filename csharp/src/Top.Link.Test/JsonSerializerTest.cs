using System;
using System.Collections.Generic;
using System.Text;
using NUnit.Framework;
using Taobao.Top.Link.Remoting;
using Taobao.Top.Link.Remoting.Serialization;
using Taobao.Top.Link.Remoting.Serialization.Json;

namespace Taobao.Top.Link.Test
{
    [TestFixture]
    public class JsonSerializerTest
    {
        private static ISerializer jsonSerializer = new CrossLanguageJsonSerializer();

        [TestCase]
        public void TypeTest()
        {
            Console.WriteLine(typeof(string[]));
            Console.WriteLine(typeof(Entity[]));
            var now = DateTime.Now;
            Assert.AreEqual(now, new DateTime(now.Ticks));
        }

        [TestCase]
        public void MethodCallTest()
        {
            MethodCall call1 = new MethodCall();
            call1.MethodName = "echo";
            call1.TypeName = "serviceType";
            call1.Uri = "uri";
            call1.Args = new Object[] {
		        "abc中文",
		        (byte) 1,
		        (double) 1.1,
		        (float) 1.2,
		        1,
		        1L,
		        (short) 1,
		        DateTime.Now,
		        GetDictionary(),
		        GetEntity(),
		        new string[] { "abc" } 
            };
            call1.MethodSignature = new Type[] {
		        typeof(string),
		        typeof(byte),
		        typeof(double),
		        typeof(float),
		        typeof(int),
		        typeof(long),
		        typeof(short),
		        typeof(DateTime),
		        typeof(IDictionary<string,string>),
		        typeof(Entity),
		        typeof(string[])
            };

            byte[] ret = jsonSerializer.SerializeMethodCall(call1);
            Console.WriteLine(Encoding.UTF8.GetString(ret));

            MethodCall call2 = jsonSerializer.DeserializeMethodCall(ret);
            Assert.AreEqual(call1.MethodName, call2.MethodName);
            Assert.AreEqual(call1.TypeName, call2.TypeName);
            Assert.AreEqual(call1.Uri, call2.Uri);
            Assert.AreEqual(call1.Args.Length, call2.Args.Length);
            for (int i = 0; i < call1.Args.Length; i++)
            {
                Assert.AreEqual(call1.Args[i].GetType(), call2.Args[i].GetType());
                Assert.AreEqual(call1.MethodSignature[i], call2.MethodSignature[i]);
                Assert.AreEqual(call1.Args[i].ToString(), call2.Args[i].ToString());
            }
            foreach (object arg in call2.Args)
                Console.WriteLine(string.Format("{0}|{1}", arg.GetType(), arg));
        }

        [TestCase]
        public void MethodReturnTest()
        {
            MethodReturn return1 = new MethodReturn();
            return1.Exception = new LinkException("error", new NullReferenceException());
            return1.ReturnValue = GetEntity();

            byte[] ret = jsonSerializer.SerializeMethodReturn(return1);
            Console.WriteLine(Encoding.UTF8.GetString(ret));

            MethodReturn return2 = jsonSerializer.DeserializeMethodReturn(ret, typeof(Entity));
            Console.WriteLine(return2.Exception.Message);
            Assert.AreEqual(return1.Exception.Message, return2.Exception.Message);
            Assert.AreEqual(return1.ReturnValue.GetType(), return2.ReturnValue.GetType());
            Assert.AreEqual(((Entity)return1.ReturnValue).String, ((Entity)return2.ReturnValue).String);
            Assert.AreEqual(((Entity)return1.ReturnValue).Dictionary, ((Entity)return2.ReturnValue).Dictionary);
            Assert.AreEqual(((Entity)return1.ReturnValue).Dictionary.Count, ((Entity)return2.ReturnValue).Dictionary.Count);
            Assert.AreEqual(((Entity)return1.ReturnValue).Array[0], ((Entity)return2.ReturnValue).Array[0]);
        }

        private IDictionary<String, String> GetDictionary()
        {
            IDictionary<string, string> map = new Dictionary<string, string>();
            map.Add("k", "k");
            return map;
        }
        private Entity GetEntity()
        {
            Entity e = new Entity();
            e.String = "string";
            e.Dictionary = this.GetDictionary();
            e.Array = new String[] { "abc" };
            return e;
        }

        public class Entity
        {
            public string String { get; set; }
            public IDictionary<String, String> Dictionary { get; set; }
            public string[] Array { get; set; }
        }
    }
}