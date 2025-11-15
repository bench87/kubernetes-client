package com.goyeau.istio.models.authorizationpolicy

import scala.compiletime.*
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
enum Action:
  case ALLOW
  case DENY
  case AUDIT
  case CUSTOM

object Action:
  given Configuration = Configuration.default
  given Decoder[Action] = Decoder.derivedConfigured
  given Encoder[Action] = Encoder.AsObject.derivedConfigured

// Sealed Traits for OneOf Fields
sealed trait StringMatch
object StringMatch:
  case class Exact(value: String) extends StringMatch
  case class Prefix(value: String) extends StringMatch
  case class Suffix(value: String) extends StringMatch
  case class Regex(value: String) extends StringMatch
  case class Presence(value: Boolean = true) extends StringMatch

  given Configuration = Configuration.default

  given Decoder[StringMatch] = Decoder.instance { cursor =>
    cursor.downField("exact").as[String].map(Exact.apply) orElse
    cursor.downField("prefix").as[String].map(Prefix.apply) orElse
    cursor.downField("suffix").as[String].map(Suffix.apply) orElse
    cursor.downField("regex").as[String].map(Regex.apply) orElse
    cursor.downField("presence").as[Boolean].map(Presence.apply)
  }

  given Encoder[StringMatch] = Encoder.instance {
    case Exact(value) => Json.obj("exact" -> Json.fromString(value))
    case Prefix(value) => Json.obj("prefix" -> Json.fromString(value))
    case Suffix(value) => Json.obj("suffix" -> Json.fromString(value))
    case Regex(value) => Json.obj("regex" -> Json.fromString(value))
    case Presence(value) => Json.obj("presence" -> Json.fromBoolean(value))
  }

// Source represents the source identities of a request
case class Source(
  principals: Option[List[StringMatch]] = None,
  notPrincipals: Option[List[StringMatch]] = None,
  requestPrincipals: Option[List[StringMatch]] = None,
  notRequestPrincipals: Option[List[StringMatch]] = None,
  namespaces: Option[List[String]] = None,
  notNamespaces: Option[List[String]] = None,
  ipBlocks: Option[List[String]] = None,
  notIpBlocks: Option[List[String]] = None,
  remoteIpBlocks: Option[List[String]] = None,
  notRemoteIpBlocks: Option[List[String]] = None
)

object Source:
  given Configuration = Configuration.default.withSnakeCaseMemberNames
  given Decoder[Source] = Decoder.derivedConfigured
  given Encoder[Source] = Encoder.AsObject.derivedConfigured

// Operation represents the operations of a request
case class Operation(
  hosts: Option[List[StringMatch]] = None,
  notHosts: Option[List[StringMatch]] = None,
  ports: Option[List[String]] = None,
  notPorts: Option[List[String]] = None,
  methods: Option[List[StringMatch]] = None,
  notMethods: Option[List[StringMatch]] = None,
  paths: Option[List[StringMatch]] = None,
  notPaths: Option[List[StringMatch]] = None
)

object Operation:
  given Configuration = Configuration.default.withSnakeCaseMemberNames
  given Decoder[Operation] = Decoder.derivedConfigured
  given Encoder[Operation] = Encoder.AsObject.derivedConfigured

// Condition represents the conditions in a when clause
case class Condition(
  key: String,
  values: Option[List[StringMatch]] = None,
  notValues: Option[List[StringMatch]] = None
)

object Condition:
  given Configuration = Configuration.default.withSnakeCaseMemberNames
  given Decoder[Condition] = Decoder.derivedConfigured
  given Encoder[Condition] = Encoder.AsObject.derivedConfigured

// Rule represents a single authorization rule
case class Rule(
  from: Option[List[Source]] = None,
  to: Option[List[Operation]] = None,
  when: Option[List[Condition]] = None
)

object Rule:
  given Configuration = Configuration.default
  given Decoder[Rule] = Decoder.derivedConfigured
  given Encoder[Rule] = Encoder.AsObject.derivedConfigured

// Selector for workload selection
case class WorkloadSelector(
  matchLabels: Map[String, String] = Map.empty
)

object WorkloadSelector:
  given Configuration = Configuration.default.withSnakeCaseMemberNames
  given Decoder[WorkloadSelector] = Decoder.derivedConfigured
  given Encoder[WorkloadSelector] = Encoder.AsObject.derivedConfigured

// ExtensionProvider for custom action
case class ExtensionProvider(
  name: String,
  service: Option[String] = None,
  port: Option[Int] = None,
  timeout: Option[String] = None,
  failOpen: Option[Boolean] = None,
  statusOnError: Option[Int] = None,
  pathPrefix: Option[String] = None,
  headers: Option[Map[String, String]] = None,
  includeRequestHeaders: Option[List[String]] = None,
  includeRequestHeadersWhenAllow: Option[List[String]] = None,
  includeAdditionalHeaders: Option[Map[String, String]] = None,
  includeAdditionalHeadersWhenAllow: Option[Map[String, String]] = None,
  forwardOriginalDestination: Option[Boolean] = None
)

object ExtensionProvider:
  given Configuration = Configuration.default.withSnakeCaseMemberNames
  given Decoder[ExtensionProvider] = Decoder.derivedConfigured
  given Encoder[ExtensionProvider] = Encoder.AsObject.derivedConfigured

// AuthorizationPolicySpec represents the specification of an authorization policy
case class AuthorizationPolicySpec(
  selector: Option[WorkloadSelector] = None,
  rules: Option[List[Rule]] = None,
  action: Option[Action] = None,
  provider: Option[ExtensionProvider] = None,
  targetRef: Option[PolicyTargetReference] = None,
  targetRefs: Option[List[PolicyTargetReference]] = None
)

object AuthorizationPolicySpec:
  given Configuration = Configuration.default.withSnakeCaseMemberNames
  given Decoder[AuthorizationPolicySpec] = Decoder.derivedConfigured
  given Encoder[AuthorizationPolicySpec] = Encoder.AsObject.derivedConfigured

// PolicyTargetReference for Kubernetes Gateway API integration
case class PolicyTargetReference(
  group: String,
  kind: String,
  name: String,
  namespace: Option[String] = None
)

object PolicyTargetReference:
  given Configuration = Configuration.default
  given Decoder[PolicyTargetReference] = Decoder.derivedConfigured
  given Encoder[PolicyTargetReference] = Encoder.AsObject.derivedConfigured

// Main AuthorizationPolicy Kubernetes resource model
case class AuthorizationPolicy(
  apiVersion: Option[String] = None,
  kind: Option[String] = None,
  metadata: Option[io.k8s.apimachinery.pkg.apis.meta.v1.ObjectMeta] = None,
  spec: Option[AuthorizationPolicySpec],
  status: Option[Unit] = None
)

object AuthorizationPolicy:
  given Configuration = Configuration.default
  given Decoder[AuthorizationPolicy] = Decoder.derivedConfigured
  given Encoder[AuthorizationPolicy] = Encoder.AsObject.derivedConfigured

// AuthorizationPolicyList for Kubernetes list operations
case class AuthorizationPolicyList(
  apiVersion: Option[String] = None,
  kind: Option[String] = None,
  metadata: Option[io.k8s.apimachinery.pkg.apis.meta.v1.ListMeta] = None,
  items: List[AuthorizationPolicy]
)

object AuthorizationPolicyList:
  given Configuration = Configuration.default
  given Decoder[AuthorizationPolicyList] = Decoder.derivedConfigured
  given Encoder[AuthorizationPolicyList] = Encoder.AsObject.derivedConfigured

// AuthorizationPolicies collection model (for internal use)
case class AuthorizationPolicies(
  namespaceToPolicies: Map[String, List[AuthorizationPolicy]],
  rootNamespace: String
)

object AuthorizationPolicies:
  given Configuration = Configuration.default.withSnakeCaseMemberNames
  given Decoder[AuthorizationPolicies] = Decoder.derivedConfigured
  given Encoder[AuthorizationPolicies] = Encoder.AsObject.derivedConfigured

// AuthorizationPoliciesResult model (for internal use)
case class AuthorizationPoliciesResult(
  custom: List[AuthorizationPolicy] = List.empty,
  deny: List[AuthorizationPolicy] = List.empty,
  allow: List[AuthorizationPolicy] = List.empty,
  audit: List[AuthorizationPolicy] = List.empty
)

object AuthorizationPoliciesResult:
  given Configuration = Configuration.default
  given Decoder[AuthorizationPoliciesResult] = Decoder.derivedConfigured
  given Encoder[AuthorizationPoliciesResult] = Encoder.AsObject.derivedConfigured

