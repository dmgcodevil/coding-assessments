package io.citrine.assessment.endpoint.json

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import io.citrine.assessment.endpoint.Result
import spray.json.{DefaultJsonProtocol, JsObject, JsString, JsValue, RootJsonFormat}

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit object ResultFormat extends RootJsonFormat[Result] {
    override def read(json: JsValue): Result = {
      throw new UnsupportedOperationException("read operation is not supported")
    }

    override def write(obj: Result): JsValue =
      JsObject(Map(
        "unit_name" -> JsString(obj.unitName),
        "multiplication_factor" -> JsString("%.14f".format(obj.multiplicationFactor)),
      ))
  }
}
