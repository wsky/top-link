using System;
using System.Collections.Generic;
using System.Text;

namespace Taobao.Top.Link.Remoting.Serialization.Json
{
    public class CrossLanguageSerializationFactory : ISerializationFactory
    {
        private CrossLanguageJsonSerializer _jsonSerializer = new CrossLanguageJsonSerializer();
        public ISerializer Get(object format)
        {
            return this._jsonSerializer;
        }
    }
}
