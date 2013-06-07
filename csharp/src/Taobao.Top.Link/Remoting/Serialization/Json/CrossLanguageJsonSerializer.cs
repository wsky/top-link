using System;
using System.Collections.Generic;
using System.Globalization;
using System.IO;
using System.Text;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;

namespace Taobao.Top.Link.Remoting.Serialization.Json
{
    /// <summary>cross-language remoting serializer, take care of type
    /// </summary>
    public class CrossLanguageJsonSerializer : ISerializer
    {
        public string Name { get { return "json"; } }
        public byte[] SerializeMethodCall(MethodCall methodCall)
        {
            var wrapper = new MethodCallWrapper(methodCall);
            wrapper.Args = methodCall.Args;
            wrapper.MethodSignature = new string[
                methodCall.MethodSignature != null ? methodCall.MethodSignature.Length : 0];
            for (int i = 0; i < methodCall.MethodSignature.Length; i++)
                wrapper.MethodSignature[i] = this.parseTypeName(methodCall.MethodSignature[i]);
            return this.ToJsonBytes(wrapper);
        }
        public MethodCall DeserializeMethodCall(byte[] input)
        {
            JObject obj = this.ToJsonObject(input);
            var methodCall = new MethodCall();
            methodCall.MethodName = obj.GetValue("MethodName").ToObject<string>();
            methodCall.TypeName = obj.GetValue("TypeName").ToObject<string>();
            methodCall.Uri = obj.GetValue("Uri").ToObject<string>();

            IList<Type> types = new List<Type>();
            foreach (JToken token in obj.GetValue("MethodSignature").AsJEnumerable())
                types.Add(this.parseType(token.ToObject<string>()));
            methodCall.MethodSignature = new Type[types.Count];
            types.CopyTo(methodCall.MethodSignature, 0);

            var i = 0;
            IList<object> args = new List<object>();
            foreach (JToken token in obj.GetValue("Args").AsJEnumerable())
                args.Add(token.ToObject(methodCall.MethodSignature[i++]));
            methodCall.Args = new object[args.Count];
            args.CopyTo(methodCall.Args, 0);

            return methodCall;
        }
        public byte[] SerializeMethodReturn(MethodReturn methodReturn)
        {
            var wrapper = new MethodReturnWrapper();
            wrapper.ReturnValue = methodReturn.ReturnValue;
            wrapper.Exception = methodReturn.Exception != null ?
                    methodReturn.Exception.Message :
                    null;
            return this.ToJsonBytes(wrapper);
        }
        public MethodReturn DeserializeMethodReturn(byte[] input, Type returnType)
        {
            JToken t;
            JObject obj = this.ToJsonObject(input);
            MethodReturn methodReturn = new MethodReturn();

            if (obj.TryGetValue("ReturnValue", out t))
                methodReturn.ReturnValue = t.ToObject(returnType);

            var ex = obj.TryGetValue("Exception", out t) ? t.ToObject<string>() : null;
            if (!string.IsNullOrEmpty(ex))
                methodReturn.Exception = new Exception(ex);

            return methodReturn;
        }

        private string parseTypeName(Type type)
        {
            if (typeof(string).Equals(type))
                return "";
            if (typeof(byte).Equals(type))
                return "b";
            if (typeof(Byte).Equals(type))
                return "B";
            if (typeof(double).Equals(type))
                return "d";
            if (typeof(Double).Equals(type))
                return "D";
            if (typeof(float).Equals(type))
                return "f";
            if (typeof(int).Equals(type))
                return "i";
            if (typeof(long).Equals(type))
                return "l";
            if (typeof(short).Equals(type))
                return "s";
            if (typeof(DateTime).Equals(type))
                return "t";
            if (typeof(IDictionary<string, string>).Equals(type)
                || typeof(IDictionary<string, string>).IsAssignableFrom(type))
                return "m";
            if (type.IsArray)
                return string.Format("[{0}", this.parseTypeName(type.GetElementType()));
            return type.FullName;
        }
        private Type parseType(string typeName)
        {
            if ("".Equals(typeName))
                return typeof(string);
            if ("b".Equals(typeName))
                return typeof(byte);
            if ("B".Equals(typeName))
                return typeof(Byte);
            if ("d".Equals(typeName))
                return typeof(double);
            if ("D".Equals(typeName))
                return typeof(Double);
            if ("f".Equals(typeName))
                return typeof(float);
            if ("i".Equals(typeName))
                return typeof(int);
            if ("l".Equals(typeName))
                return typeof(long);
            if ("s".Equals(typeName))
                return typeof(short);
            if ("t".Equals(typeName))
                return typeof(DateTime);
            if ("m".Equals(typeName))
                return typeof(IDictionary<string, string>);
            if (typeName[0] == '[')
                // c# array: System.String[]
                typeName = string.Format("{0}[]", this.parseType(this.GetElementTypeName(typeName)).FullName);
            //TODO:support special assembly for searching
            foreach (var a in AppDomain.CurrentDomain.GetAssemblies())
            {
                var t = a.GetType(typeName, false);
                if (t != null)
                    return t;
            }
            return Type.GetType(typeName, true);
        }
        private byte[] ToJsonBytes(object value)
        {
            //instead of JsonConvert.SerializeObject
            //https://github.com/JamesNK/Newtonsoft.Json/blob/master/Src/Newtonsoft.Json/JsonConvert.cs#L588
            JsonSerializer jsonSerializer = JsonSerializer.CreateDefault();
            //using (var s = new MemoryStream())
            //using (var w = new StreamWriter(s, Encoding.UTF8))
            //MemoryStream not work?
            var s = new StringBuilder();
            var w = new StringWriter(s);
            using (var jsonWriter = new JsonTextWriter(w))
            {
                jsonWriter.Formatting = Formatting.None;
                jsonSerializer.Serialize(jsonWriter, value);
                return Encoding.UTF8.GetBytes(s.ToString());
                //return s.ToArray();
            }
        }
        private JObject ToJsonObject(byte[] input)
        {
            //instead of JsonConvert.DeserializeObject
            //https://github.com/JamesNK/Newtonsoft.Json/blob/master/Src/Newtonsoft.Json/JsonConvert.cs#L786
            JsonSerializer jsonSerializer = JsonSerializer.CreateDefault();
            using (var s = new MemoryStream(input))
            using (var r = new StreamReader(s, Encoding.UTF8))
                return jsonSerializer.Deserialize(new JsonTextReader(r), null) as JObject;
        }
        private string GetElementTypeName(string typeName)
        {
            return typeName.Substring(1);
        }
        public class MethodCallWrapper : MethodCall
        {
            public new String[] MethodSignature { get; set; }
            public MethodCallWrapper() { }
            public MethodCallWrapper(MethodCall origin)
            {
                this.MethodName = origin.MethodName;
                this.TypeName = origin.TypeName;
                this.Uri = origin.Uri;
            }
        }
        public class MethodReturnWrapper : MethodReturn
        {
            public new string Exception { get; set; }
        }
    }
}