using System;
using System.Collections.Generic;
using System.Text;

namespace Taobao.Top.Link.Remoting.Serialization.Json
{
    /// <summary>cross-language remoting serializer, take care of type
    /// </summary>
    public class CrossLanguageJsonSerializer : ISerializer
    {
        public string Name { get { return "json"; } }
        public byte[] SerializeMethodCall(MethodCall methodCall)
        {
            throw new NotImplementedException();
        }
        public MethodReturn DeserializeMethodReturn(byte[] input, Type returnType)
        {
            throw new NotImplementedException();
        }
        public byte[] SerializeMethodReturn(MethodReturn methodReturn)
        {
            throw new NotImplementedException();
        }
        public MethodCall DeserializeMethodCall(byte[] input)
        {
            throw new NotImplementedException();
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
            //if (typeof(Map.class.equals(type) || Map.class.isAssignableFrom(type))
            //    return "m";
            //if (typeof(type.isArray())
            //    return String.format("[%s", this.parseTypeName(type.getComponentType()));
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
            //if ("m".Equals(typeName))
            //    return HashMap.class;
            //if (typeName.charAt(0) == '[')
            //    // java array: [Ljava.lang.String
            //    typeName = String.format("[L%s;",
            //            this.parseType(this.getComponentTypeName(typeName)).getName());
            return Type.GetType(typeName);
        }
    }
}