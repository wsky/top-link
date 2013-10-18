using System;
using System.Collections.Generic;
using System.Text;
using NUnit.Framework;
using Taobao.Top.Link.Util;

namespace Taobao.Top.Link.Test
{
    [TestFixture]
    public class GZIPHelperTest
    {
        [TestCase]
        public void ZipTest()
        {
            var str = "hi";
            var data = Encoding.UTF8.GetBytes(str);
            Console.WriteLine(data.Length);
            var zipped = GZIPHelper.Zip(data);

            Console.WriteLine(zipped.Length);
            foreach (var b in zipped)
                Console.Write(b + ",");
            Console.WriteLine();

            Assert.AreEqual(str, Encoding.UTF8.GetString(GZIPHelper.Unzip(zipped)));
        }

        [TestCase]
        public void UnzipTest()
        {
            //from java gzip
            var data = new byte[] { 31, (-117) + 256, 8, 0, 0, 0, 0, 0, 0, 0, (-53) + 256, (-56) + 256, 4, 0, (-84) + 256, 42, (-109) + 256, (-40) + 256, 2, 0, 0, 0 };
            var str = Encoding.UTF8.GetString(GZIPHelper.Unzip(data));
            Console.WriteLine(str);
            Assert.AreEqual("hi", str);
        }
    }
}
