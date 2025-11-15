package com.goyeau.istio.models.sidecar

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
enum CaptureMode:
  case DEFAULT
  case IPTABLES
  case NONE

object CaptureMode:
  given Configuration = Configuration.default
  given Decoder[CaptureMode] = Decoder.derivedConfigured
  given Encoder[CaptureMode] = Encoder.AsObject.derivedConfigured

enum OutboundTrafficPolicyMode:
  case REGISTRY_ONLY
  case ALLOW_ANY

object OutboundTrafficPolicyMode:
  given Configuration = Configuration.default
  given Decoder[OutboundTrafficPolicyMode] = Decoder.derivedConfigured
  given Encoder[OutboundTrafficPolicyMode] = Encoder.AsObject.derivedConfigured

// Case Classes
case class SidecarSpec(
  workloadSelector: Option[WorkloadSelector] = None,
  ingress: Option[List[IstioIngressListener]] = None,
  egress: Option[List[IstioEgressListener]] = None,
  inboundConnectionPool: Option[ConnectionPoolSettings] = None,
  outboundTrafficPolicy: Option[OutboundTrafficPolicy] = None
)
object SidecarSpec:
  given Configuration = Configuration.default
  given Decoder[SidecarSpec] = Decoder.derivedConfigured
  given Encoder[SidecarSpec] = Encoder.AsObject.derivedConfigured

case class WorkloadSelector(
  labels: Option[Map[String, String]] = None
)
object WorkloadSelector:
  given Configuration = Configuration.default
  given Decoder[WorkloadSelector] = Decoder.derivedConfigured
  given Encoder[WorkloadSelector] = Encoder.AsObject.derivedConfigured

case class IstioIngressListener(
  port: SidecarPort,
  bind: Option[String] = None,
  captureMode: Option[CaptureMode] = None,
  defaultEndpoint: Option[String] = None,
  tls: Option[ServerTLSSettings] = None,
  connectionPool: Option[ConnectionPoolSettings] = None
)
object IstioIngressListener:
  given Configuration = Configuration.default
  given Decoder[IstioIngressListener] = Decoder.derivedConfigured
  given Encoder[IstioIngressListener] = Encoder.AsObject.derivedConfigured

case class IstioEgressListener(
  port: Option[SidecarPort] = None,
  bind: Option[String] = None,
  captureMode: Option[CaptureMode] = None,
  hosts: List[String]
)
object IstioEgressListener:
  given Configuration = Configuration.default
  given Decoder[IstioEgressListener] = Decoder.derivedConfigured
  given Encoder[IstioEgressListener] = Encoder.AsObject.derivedConfigured

case class SidecarPort(
  number: Option[Int] = None,
  protocol: Option[String] = None,
  name: Option[String] = None
)
object SidecarPort:
  given Configuration = Configuration.default
  given Decoder[SidecarPort] = Decoder.derivedConfigured
  given Encoder[SidecarPort] = Encoder.AsObject.derivedConfigured

case class OutboundTrafficPolicy(
  mode: Option[OutboundTrafficPolicyMode] = None
)
object OutboundTrafficPolicy:
  given Configuration = Configuration.default
  given Decoder[OutboundTrafficPolicy] = Decoder.derivedConfigured
  given Encoder[OutboundTrafficPolicy] = Encoder.AsObject.derivedConfigured

case class ConnectionPoolSettings(
  // Fields are not specified in the provided document
)
object ConnectionPoolSettings:
  given Configuration = Configuration.default
  given Decoder[ConnectionPoolSettings] = Decoder.derivedConfigured
  given Encoder[ConnectionPoolSettings] = Encoder.AsObject.derivedConfigured

case class ServerTLSSettings(
  // Fields are not specified in the provided document
)
object ServerTLSSettings:
  given Configuration = Configuration.default
  given Decoder[ServerTLSSettings] = Decoder.derivedConfigured
  given Encoder[ServerTLSSettings] = Encoder.AsObject.derivedConfigured

case class Sidecar(
    apiVersion: Option[String] = None,
    kind: Option[String] = None,
    metadata: Option[io.k8s.apimachinery.pkg.apis.meta.v1.ObjectMeta] = None,
    spec: Option[SidecarSpec],
    status: Option[Unit] = None
)
object Sidecar:
  given Configuration = Configuration.default
  given Decoder[Sidecar] = Decoder.derivedConfigured
  given Encoder[Sidecar] = Encoder.AsObject.derivedConfigured

case class SidecarList(
    apiVersion: Option[String] = None,
    kind: Option[String] = None,
    metadata: Option[io.k8s.apimachinery.pkg.apis.meta.v1.ListMeta] = None,
    items: List[Sidecar]
)

object SidecarList:
  given Configuration = Configuration.default
  given Decoder[SidecarList] = Decoder.derivedConfigured
  given Encoder[SidecarList] = Encoder.AsObject.derivedConfigured

