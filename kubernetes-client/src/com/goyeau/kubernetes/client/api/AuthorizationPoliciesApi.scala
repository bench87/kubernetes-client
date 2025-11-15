package com.goyeau.kubernetes.client.api

import cats.effect.Async
import com.goyeau.istio.models.authorizationpolicy.*
import com.goyeau.kubernetes.client.KubeConfig
import com.goyeau.kubernetes.client.operation.*
import io.circe.*
import org.http4s.Uri
import org.http4s.client.Client
import org.http4s.headers.Authorization
import org.http4s.implicits.*

private[client] object AuthorizationPoliciesApi {
  enum ApiVersion(val segment: String) {
    case V1 extends ApiVersion("v1")
    case V1Beta1 extends ApiVersion("v1beta1")
  }

  private[client] def baseUri(version: ApiVersion): Uri =
    uri"/apis" / "security.istio.io" / version.segment
}

private[client] class AuthorizationPoliciesApi[F[_]](
    val httpClient: Client[F],
    val config: KubeConfig[F],
    val authorization: Option[F[Authorization]],
    private val apiVersion: AuthorizationPoliciesApi.ApiVersion = AuthorizationPoliciesApi.ApiVersion.V1Beta1
)(implicit
    val F: Async[F],
    val listDecoder: Decoder[AuthorizationPolicyList],
    val resourceDecoder: Decoder[AuthorizationPolicy],
    encoder: Encoder[AuthorizationPolicy]
) extends Listable[F, AuthorizationPolicyList]
    with Watchable[F, AuthorizationPolicy] {
  import AuthorizationPoliciesApi.*

  val resourceUri: Uri = baseUri(apiVersion) / "authorizationpolicies"

  def namespace(namespace: String): NamespacedAuthorizationPoliciesApi[F] =
    new NamespacedAuthorizationPoliciesApi(httpClient, config, authorization, namespace, apiVersion)
}

private[client] class NamespacedAuthorizationPoliciesApi[F[_]](
    val httpClient: Client[F],
    val config: KubeConfig[F],
    val authorization: Option[F[Authorization]],
    namespace: String,
    private val apiVersion: AuthorizationPoliciesApi.ApiVersion
)(implicit
    val F: Async[F],
    val resourceEncoder: Encoder[AuthorizationPolicy],
    val resourceDecoder: Decoder[AuthorizationPolicy],
    val listDecoder: Decoder[AuthorizationPolicyList]
) extends Creatable[F, AuthorizationPolicy]
    with Replaceable[F, AuthorizationPolicy]
    with Gettable[F, AuthorizationPolicy]
    with Listable[F, AuthorizationPolicyList]
    with Deletable[F]
    with DeletableTerminated[F]
    with GroupDeletable[F]
    with Watchable[F, AuthorizationPolicy] {
  import AuthorizationPoliciesApi.*

  val resourceUri: Uri = baseUri(apiVersion) / "namespaces" / namespace / "authorizationpolicies"
}

