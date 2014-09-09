using System;
using System.Collections.Generic;
using System.Text;

namespace Top.Link.Remoting.Serialization
{
    public interface ISerializer
    {
        string Name { get; }
        byte[] SerializeMethodCall(MethodCall methodCall);
        MethodReturn DeserializeMethodReturn(byte[] input, Type returnType);
        byte[] SerializeMethodReturn(MethodReturn methodReturn);
        MethodCall DeserializeMethodCall(byte[] input);
    }
}