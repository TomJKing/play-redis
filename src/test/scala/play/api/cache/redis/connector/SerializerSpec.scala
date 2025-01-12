package play.api.cache.redis.connector

import java.util.Date

import play.api.inject.guice.GuiceApplicationBuilder
import play.api.cache.redis._

import org.specs2.mock.Mockito
import org.specs2.mutable.Specification

class SerializerSpec extends Specification with Mockito {
  import SerializerImplicits._

  private val system = GuiceApplicationBuilder().build().actorSystem

  private implicit val serializer: AkkaSerializer = new AkkaSerializerImpl(system)

  "AkkaEncoder" should "encode" >> {

    "byte" in {
      0xAB.toByte.encoded mustEqual "-85"
      JavaTypes.byteValue.encoded mustEqual "5"
    }

    "byte[]" in {
      JavaTypes.bytesValue.encoded mustEqual "AQID"
    }

    "char" in {
      'a'.encoded mustEqual "a"
      'b'.encoded mustEqual "b"
      'š'.encoded mustEqual "š"
    }

    "boolean" in {
      true.encoded mustEqual "true"
    }

    "short" in {
      12.toShort.toByte.encoded mustEqual "12"
    }

    "int" in {
      15.encoded mustEqual "15"
    }

    "long" in {
      144L.encoded mustEqual "144"
    }

    "float" in {
      1.23f.encoded mustEqual "1.23"
    }

    "double" in {
      3.14.encoded mustEqual "3.14"
    }

    "string" in {
      "some string".encoded mustEqual "some string"
    }

    "date" in {
      new Date(123).encoded mustEqual "rO0ABXNyAA5qYXZhLnV0aWwuRGF0ZWhqgQFLWXQZAwAAeHB3CAAAAAAAAAB7eA=="
    }

    "null" in {
      new ValueEncoder(null).encoded must throwA[UnsupportedOperationException]
    }
  }

  "AkkaDecoder" should "decode" >> {

    "byte" in {
      "-85".decoded[Byte] mustEqual 0xAB.toByte
    }

    "byte[]" in {
      "YWJj".decoded[Array[Byte]] mustEqual Array("a".head.toByte, "b".head.toByte, "c".head.toByte)
    }

    "char" in {
      "a".decoded[Char] mustEqual 'a'
      "b".decoded[Char] mustEqual 'b'
      "š".decoded[Char] mustEqual 'š'
    }

    "boolean" in {
      "true".decoded[Boolean] mustEqual true
    }

    "short" in {
      "12".decoded[Short] mustEqual 12.toShort.toByte
    }

    "int" in {
      "15".decoded[Int] mustEqual 15
    }

    "long" in {
      "144".decoded[Long] mustEqual 144L
    }

    "float" in {
      "1.23".decoded[Float] mustEqual 1.23f
    }

    "double" in {
      "3.14".decoded[Double] mustEqual 3.14
    }

    "string" in {
      "some string".decoded[String] mustEqual "some string"
    }

    "null" in {
      "".decoded[String] must beNull
    }

    "date" in {
      "rO0ABXNyAA5qYXZhLnV0aWwuRGF0ZWhqgQFLWXQZAwAAeHB3CAAAAAAAAAB7eA==".decoded[Date] mustEqual new Date(123)
    }

    "invalid type" in {
      def decoded: Date = "something".decoded[Date]
      decoded must throwA[IllegalArgumentException]
    }
  }
}
