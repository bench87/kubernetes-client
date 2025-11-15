package com.goyeau.istio.models.virtualService

import scala.compiletime.*
import scala.concurrent.duration.Duration
import scala.deriving.Mirror

import cats.syntax.all.*
import io.circe.*
import io.circe.Codec
import io.circe.Decoder
import io.circe.DecodingFailure
import io.circe.DecodingFailure.Reason.MissingField
import io.circe.Encoder
import io.circe.derivation.Configuration
import io.circe.derivation.ConfiguredDecoder
import io.circe.generic.semiauto.*


// Enums
enum RedirectPortSelection:
  case FROM_PROTOCOL_DEFAULT
  case FROM_REQUEST_PORT

object RedirectPortSelection:
  given Configuration = Configuration.default
  given Decoder[RedirectPortSelection] = Decoder.derivedConfigured
  given Encoder[RedirectPortSelection] = Encoder.AsObject.derivedConfigured

enum UnmatchedPreflights:
  case UNSPECIFIED
  case FORWARD
  case IGNORE

object UnmatchedPreflights:
  given Configuration = Configuration.default
  given Decoder[UnmatchedPreflights] = Decoder.derivedConfigured
  given Encoder[UnmatchedPreflights] = Encoder.AsObject.derivedConfigured

// Sealed Traits for OneOf Fields
sealed trait StringMatch
object StringMatch:
  case class Exact(value: String) extends StringMatch
  case class Prefix(value: String) extends StringMatch
  case class Regex(value: String) extends StringMatch

  given Configuration = Configuration.default
  private val exactKey  = "exact"
  private val prefixKey = "prefix"
  private val regexKey  = "regex"

  given Decoder[StringMatch] = Decoder.instance { cursor =>
    val value = cursor.value
    value.as[String].map(Exact(_)).orElse {
      cursor.downField(exactKey).as[String].map(Exact(_))
        .orElse(cursor.downField(prefixKey).as[String].map(Prefix(_)))
        .orElse(cursor.downField(regexKey).as[String].map(Regex(_)))
    }
  }

  given Encoder[StringMatch] = Encoder.instance {
    case Exact(value)  => Json.obj(exactKey -> Json.fromString(value))
    case Prefix(value) => Json.obj(prefixKey -> Json.fromString(value))
    case Regex(value)  => Json.obj(regexKey -> Json.fromString(value))
  }

sealed trait HTTPBody
object HTTPBody:
  case class StringBody(value: String) extends HTTPBody
  case class BytesBody(value: Array[Byte]) extends HTTPBody

  given Configuration = Configuration.default
  given Decoder[StringBody] = Decoder.derivedConfigured
  given Encoder[StringBody] = Encoder.AsObject.derivedConfigured
  given Decoder[BytesBody] = Decoder.derivedConfigured
  given Encoder[BytesBody] = Encoder.AsObject.derivedConfigured

  given Decoder[HTTPBody] = List[Decoder[HTTPBody]](
    Decoder[StringBody].widen,
    Decoder[BytesBody].widen
  ).reduceLeft(_.or(_))

  given Encoder[HTTPBody] = Encoder.instance {
    case sb: StringBody => Encoder[StringBody].apply(sb)
    case bb: BytesBody  => Encoder[BytesBody].apply(bb)
  }

sealed trait PortSetting
object PortSetting:
  case class PortNumber(value: Int) extends PortSetting
  case class PortSelection(selection: RedirectPortSelection) extends PortSetting

  given Configuration = Configuration.default
  given Decoder[PortNumber] = Decoder.derivedConfigured
  given Encoder[PortNumber] = Encoder.AsObject.derivedConfigured
  given Decoder[PortSelection] = Decoder.derivedConfigured
  given Encoder[PortSelection] = Encoder.AsObject.derivedConfigured

  given Decoder[PortSetting] = List[Decoder[PortSetting]](
    Decoder[PortNumber].widen,
    Decoder[PortSelection].widen
  ).reduceLeft(_.or(_))

  given Encoder[PortSetting] = Encoder.instance {
    case pn: PortNumber     => Encoder[PortNumber].apply(pn)
    case ps: PortSelection  => Encoder[PortSelection].apply(ps)
  }

sealed trait FaultAbortStatus
object FaultAbortStatus:
  case class HTTPStatus(code: Int) extends FaultAbortStatus
  case class GRPCStatus(code: String) extends FaultAbortStatus

  given Configuration = Configuration.default
  given Decoder[HTTPStatus] = Decoder.derivedConfigured
  given Encoder[HTTPStatus] = Encoder.AsObject.derivedConfigured
  given Decoder[GRPCStatus] = Decoder.derivedConfigured
  given Encoder[GRPCStatus] = Encoder.AsObject.derivedConfigured

  given Decoder[FaultAbortStatus] = List[Decoder[FaultAbortStatus]](
    Decoder[HTTPStatus].widen,
    Decoder[GRPCStatus].widen
  ).reduceLeft(_.or(_))

  given Encoder[FaultAbortStatus] = Encoder.instance {
    case hs: HTTPStatus  => Encoder[HTTPStatus].apply(hs)
    case gs: GRPCStatus  => Encoder[GRPCStatus].apply(gs)
  }

sealed trait HTTPRouteAction
object HTTPRouteAction:
  case class RouteDestinations(destinations: List[HTTPRouteDestination]) extends HTTPRouteAction
  case class Redirect(redirect: HTTPRedirect) extends HTTPRouteAction
  case class DirectResponse(response: HTTPDirectResponse) extends HTTPRouteAction
  case class DelegateAction(delegate: Delegate) extends HTTPRouteAction

  given Configuration = Configuration.default
  given Decoder[RouteDestinations] = Decoder.instance { cursor =>
    cursor
      .downField("destinations")
      .as[List[HTTPRouteDestination]]
      .map(RouteDestinations(_))
      .orElse(cursor.as[List[HTTPRouteDestination]].map(RouteDestinations(_)))
  }
  given Encoder[RouteDestinations] = Encoder.instance { rd =>
    Json.obj("destinations" -> Encoder[List[HTTPRouteDestination]].apply(rd.destinations))
  }
  given Decoder[Redirect] = Decoder.derivedConfigured
  given Encoder[Redirect] = Encoder.AsObject.derivedConfigured
  given Decoder[DirectResponse] = Decoder.derivedConfigured
  given Encoder[DirectResponse] = Encoder.AsObject.derivedConfigured
  given Decoder[DelegateAction] = Decoder.derivedConfigured
  given Encoder[DelegateAction] = Encoder.AsObject.derivedConfigured

  given Decoder[HTTPRouteAction] = List[Decoder[HTTPRouteAction]](
    Decoder[RouteDestinations].widen,
    Decoder[Redirect].widen,
    Decoder[DirectResponse].widen,
    Decoder[DelegateAction].widen
  ).reduceLeft(_.or(_))

  given Encoder[HTTPRouteAction] = Encoder.instance {
    case rd: RouteDestinations => Encoder[RouteDestinations].apply(rd)
    case r: Redirect           => Encoder[Redirect].apply(r)
    case dr: DirectResponse    => Encoder[DirectResponse].apply(dr)
    case da: DelegateAction    => Encoder[DelegateAction].apply(da)
  }

// Case Classes
case class VirtualServiceSpec(
  hosts: Option[List[String]] = None,
  gateways: Option[List[String]] = None,
  http: Option[List[HTTPRoute]] = None,
  tls: Option[List[TLSRoute]] = None,
  tcp: Option[List[TCPRoute]] = None,
  exportTo: Option[List[String]] = None
)
object VirtualServiceSpec:
  given Configuration = Configuration.default
  given Decoder[VirtualServiceSpec] = Decoder.derivedConfigured
  given Encoder[VirtualServiceSpec] = Encoder.AsObject.derivedConfigured

case class VirtualService(
    apiVersion: Option[String] = None,
    kind: Option[String] = None,
    metadata: Option[io.k8s.apimachinery.pkg.apis.meta.v1.ObjectMeta] = None,
    spec: Option[VirtualServiceSpec],
    status: Option[Unit] = None
)
object VirtualService:
  given Configuration = Configuration.default
  given Decoder[VirtualService] = Decoder.derivedConfigured
  given Encoder[VirtualService] = Encoder.AsObject.derivedConfigured

case class VirtualServiceList(
    apiVersion: Option[String] = None,
    kind: Option[String] = None,
    metadata: Option[io.k8s.apimachinery.pkg.apis.meta.v1.ListMeta] = None,
    items: List[VirtualService]
)
object VirtualServiceList:
  given Configuration = Configuration.default
  given Decoder[VirtualServiceList] = Decoder.derivedConfigured
  given Encoder[VirtualServiceList] = Encoder.AsObject.derivedConfigured

case class Destination(
  host: String,
  subset: Option[String] = None,
  port: Option[PortSelector] = None
)
object Destination:
  given Configuration = Configuration.default
  given Decoder[Destination] = Decoder.derivedConfigured
  given Encoder[Destination] = Encoder.AsObject.derivedConfigured

case class HTTPRoute(
  name: Option[String] = None,
  `match`: Option[List[HTTPMatchRequest]] = None,
  route: HTTPRouteAction,
  redirect: Option[HTTPRedirect] = None,
  directResponse: Option[HTTPDirectResponse] = None,
  delegate: Option[Delegate] = None,
  rewrite: Option[HTTPRewrite] = None,
  timeout: Option[String] = None, // Duration as string
  retries: Option[HTTPRetry] = None,
  fault: Option[HTTPFaultInjection] = None,
  mirror: Option[Destination] = None,
  mirrors: Option[List[HTTPMirrorPolicy]] = None,
  mirrorPercentage: Option[Percent] = None,
  corsPolicy: Option[CorsPolicy] = None,
  headers: Option[Headers] = None
)
object HTTPRoute:
  given Configuration = Configuration.default
  given Decoder[HTTPRoute] = Decoder.derivedConfigured
  given Encoder[HTTPRoute] = Encoder.AsObject.derivedConfigured

case class Delegate(
  name: Option[String] = None,
  namespace: Option[String] = None
)
object Delegate:
  given Configuration = Configuration.default
  given Decoder[Delegate] = Decoder.derivedConfigured
  given Encoder[Delegate] = Encoder.AsObject.derivedConfigured

case class Headers(
  request: Option[HeaderOperations] = None,
  response: Option[HeaderOperations] = None
)
object Headers:
  given Configuration = Configuration.default
  given Decoder[Headers] = Decoder.derivedConfigured
  given Encoder[Headers] = Encoder.AsObject.derivedConfigured

case class TLSRoute(
  matchAttributes: List[TLSMatchAttributes],
  route: Option[List[RouteDestination]] = None
)
object TLSRoute:
  given Configuration = Configuration.default
  given Decoder[TLSRoute] = Decoder.derivedConfigured
  given Encoder[TLSRoute] = Encoder.AsObject.derivedConfigured

case class TCPRoute(
  matchAttributes: Option[List[L4MatchAttributes]] = None,
  route: Option[List[RouteDestination]] = None
)
object TCPRoute:
  given Configuration = Configuration.default
  given Decoder[TCPRoute] = Decoder.derivedConfigured
  given Encoder[TCPRoute] = Encoder.AsObject.derivedConfigured

case class HTTPMatchRequest(
  name: Option[String] = None,
  uri: Option[StringMatch] = None,
  scheme: Option[StringMatch] = None,
  method: Option[StringMatch] = None,
  authority: Option[StringMatch] = None,
  headers: Option[Map[String, StringMatch]] = None,
  port: Option[Int] = None,
  sourceLabels: Option[Map[String, String]] = None,
  gateways: Option[List[String]] = None,
  queryParams: Option[Map[String, StringMatch]] = None,
  ignoreUriCase: Option[Boolean] = None,
  withoutHeaders: Option[Map[String, StringMatch]] = None,
  sourceNamespace: Option[String] = None,
  statPrefix: Option[String] = None
)
object HTTPMatchRequest:
  given Configuration = Configuration.default
  given Decoder[HTTPMatchRequest] = Decoder.instance { cursor =>
    def coerceUri(field: String): Decoder.Result[Option[StringMatch]] =
      cursor.downField(field).success match
        case None => Right(None)
        case Some(fieldCursor) => fieldCursor.as[StringMatch].map(Some(_)).leftFlatMap { originalErr =>
            fieldCursor.focus.flatMap(_.asString) match
              case Some(str) =>
                Right(Some(StringMatch.Exact(str)))
              case None => Left(originalErr)
          }

    for
      name           <- cursor.downField("name").as[Option[String]]
      uri            <- coerceUri("uri")
      scheme         <- cursor.downField("scheme").as[Option[StringMatch]]
      method         <- cursor.downField("method").as[Option[StringMatch]]
      authority      <- cursor.downField("authority").as[Option[StringMatch]]
      headers        <- cursor.downField("headers").as[Option[Map[String, StringMatch]]]
      port           <- cursor.downField("port").as[Option[Int]]
      sourceLabels   <- cursor.downField("sourceLabels").as[Option[Map[String, String]]]
      gateways       <- cursor.downField("gateways").as[Option[List[String]]]
      queryParams    <- cursor.downField("queryParams").as[Option[Map[String, StringMatch]]]
      ignoreUriCase  <- cursor.downField("ignoreUriCase").as[Option[Boolean]]
      withoutHeaders <- cursor.downField("withoutHeaders").as[Option[Map[String, StringMatch]]]
      sourceNamespace<- cursor.downField("sourceNamespace").as[Option[String]]
      statPrefix     <- cursor.downField("statPrefix").as[Option[String]]
    yield HTTPMatchRequest(
      name,
      uri,
      scheme,
      method,
      authority,
      headers,
      port,
      sourceLabels,
      gateways,
      queryParams,
      ignoreUriCase,
      withoutHeaders,
      sourceNamespace,
      statPrefix,
    )
  }
  given Encoder[HTTPMatchRequest] = Encoder.AsObject.derivedConfigured

case class HTTPRouteDestination(
  destination: Destination,
  weight: Option[Int] = None,
  headers: Option[Headers] = None
)
object HTTPRouteDestination:
  given Configuration = Configuration.default
  given Decoder[HTTPRouteDestination] = Decoder.derivedConfigured
  given Encoder[HTTPRouteDestination] = Encoder.AsObject.derivedConfigured

case class RouteDestination(
  destination: Destination,
  weight: Option[Int] = None
)
object RouteDestination:
  given Configuration = Configuration.default
  given Decoder[RouteDestination] = Decoder.derivedConfigured
  given Encoder[RouteDestination] = Encoder.AsObject.derivedConfigured

case class L4MatchAttributes(
  destinationSubnets: Option[List[String]] = None,
  port: Option[Int] = None,
  sourceLabels: Option[Map[String, String]] = None,
  gateways: Option[List[String]] = None,
  sourceNamespace: Option[String] = None
)
object L4MatchAttributes:
  given Configuration = Configuration.default
  given Decoder[L4MatchAttributes] = Decoder.derivedConfigured
  given Encoder[L4MatchAttributes] = Encoder.AsObject.derivedConfigured

case class TLSMatchAttributes(
  sniHosts: List[String],
  destinationSubnets: Option[List[String]] = None,
  port: Option[Int] = None,
  sourceLabels: Option[Map[String, String]] = None,
  gateways: Option[List[String]] = None,
  sourceNamespace: Option[String] = None
)
object TLSMatchAttributes:
  given Configuration = Configuration.default
  given Decoder[TLSMatchAttributes] = Decoder.derivedConfigured
  given Encoder[TLSMatchAttributes] = Encoder.AsObject.derivedConfigured

case class HTTPRedirect(
  uri: Option[String] = None,
  authority: Option[String] = None,
  portSetting: Option[PortSetting] = None,
  scheme: Option[String] = None,
  redirectCode: Option[Int] = None
)
object HTTPRedirect:
  given Configuration = Configuration.default
  given Decoder[HTTPRedirect] = Decoder.derivedConfigured
  given Encoder[HTTPRedirect] = Encoder.AsObject.derivedConfigured

case class HTTPDirectResponse(
  status: Int,
  body: Option[HTTPBody] = None
)
object HTTPDirectResponse:
  given Configuration = Configuration.default
  given Decoder[HTTPDirectResponse] = Decoder.derivedConfigured
  given Encoder[HTTPDirectResponse] = Encoder.AsObject.derivedConfigured

case class HTTPRewrite(
  uri: Option[String] = None,
  authority: Option[String] = None,
  uriRegexRewrite: Option[RegexRewrite] = None
)
object HTTPRewrite:
  given Configuration = Configuration.default
  given Decoder[HTTPRewrite] = Decoder.derivedConfigured
  given Encoder[HTTPRewrite] = Encoder.AsObject.derivedConfigured

case class RegexRewrite(
  `match`: Option[String] = None,
  rewrite: Option[String] = None
)
object RegexRewrite:
  given Configuration = Configuration.default
  given Decoder[RegexRewrite] = Decoder.derivedConfigured
  given Encoder[RegexRewrite] = Encoder.AsObject.derivedConfigured

case class HTTPRetry(
  attempts: Option[Int] = None,
  perTryTimeout: Option[String] = None, // Duration as string
  retryOn: Option[String] = None,
  retryRemoteLocalities: Option[BoolValue] = None
)
object HTTPRetry:
  given Configuration = Configuration.default
  given Decoder[HTTPRetry] = Decoder.derivedConfigured
  given Encoder[HTTPRetry] = Encoder.AsObject.derivedConfigured

case class CorsPolicy(
  allowOrigins: Option[List[StringMatch]] = None,
  allowMethods: Option[List[String]] = None,
  allowHeaders: Option[List[String]] = None,
  exposeHeaders: Option[List[String]] = None,
  maxAge: Option[String] = None, // Duration as string
  allowCredentials: Option[BoolValue] = None,
  unmatchedPreflights: Option[UnmatchedPreflights] = None
)
object CorsPolicy:
  given Configuration = Configuration.default
  given Decoder[CorsPolicy] = Decoder.derivedConfigured
  given Encoder[CorsPolicy] = Encoder.AsObject.derivedConfigured

case class HTTPFaultInjection(
  delay: Option[Delay] = None,
  abort: Option[Abort] = None
)
object HTTPFaultInjection:
  given Configuration = Configuration.default
  given Decoder[HTTPFaultInjection] = Decoder.derivedConfigured
  given Encoder[HTTPFaultInjection] = Encoder.AsObject.derivedConfigured

case class HTTPMirrorPolicy(
  destination: Destination,
  percentage: Option[Percent] = None
)
object HTTPMirrorPolicy:
  given Configuration = Configuration.default
  given Decoder[HTTPMirrorPolicy] = Decoder.derivedConfigured
  given Encoder[HTTPMirrorPolicy] = Encoder.AsObject.derivedConfigured

case class PortSelector(
  number: Option[Int] = None
)
object PortSelector:
  given Configuration = Configuration.default
  given Decoder[PortSelector] = Decoder.derivedConfigured
  given Encoder[PortSelector] = Encoder.AsObject.derivedConfigured

case class Percent(
  value: Double
)
object Percent:
  given Configuration = Configuration.default
  given Decoder[Percent] = Decoder.derivedConfigured
  given Encoder[Percent] = Encoder.AsObject.derivedConfigured

case class HeaderOperations(
  set: Option[Map[String, String]] = None,
  add: Option[Map[String, String]] = None,
  remove: Option[List[String]] = None
)
object HeaderOperations:
  given Configuration = Configuration.default
  given Decoder[HeaderOperations] = Decoder.derivedConfigured
  given Encoder[HeaderOperations] = Encoder.AsObject.derivedConfigured

case class Delay(
  fixedDelay: Option[String] = None, // Duration as string
  percentage: Option[Percent] = None,
  percent: Option[Int] = None
)
object Delay:
  given Configuration = Configuration.default
  given Decoder[Delay] = Decoder.derivedConfigured
  given Encoder[Delay] = Encoder.AsObject.derivedConfigured

case class Abort(
  status: FaultAbortStatus,
  percentage: Option[Percent] = None
)
object Abort:
  given Configuration = Configuration.default
  given Decoder[Abort] = Decoder.derivedConfigured
  given Encoder[Abort] = Encoder.AsObject.derivedConfigured

case class BoolValue(
  value: Boolean
)
object BoolValue:
  given Configuration = Configuration.default
  given Decoder[BoolValue] = Decoder.derivedConfigured
  given Encoder[BoolValue] = Encoder.AsObject.derivedConfigured

