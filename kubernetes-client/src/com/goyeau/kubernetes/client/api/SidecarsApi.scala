package com.goyeau.kubernetes.client.api

import cats.effect.Async
import com.goyeau.istio.models.sidecar.*
import com.goyeau.kubernetes.client.KubeConfig
import com.goyeau.kubernetes.client.operation.*
import io.circe.*
import org.http4s.Uri
import org.http4s.client.Client
import org.http4s.headers.Authorization
import org.http4s.implicits.*

private[client] object SidecarsApi {
  enum ApiVersion(val segment: String) {
    case V1 extends ApiVersion("v1")
    case V1Beta1 extends ApiVersion("v1beta1")
  }

  private[client] def baseUri(version: ApiVersion): Uri =
    uri"/apis" / "networking.istio.io" / version.segment
}

private[client] class SidecarsApi[F[_]](
    val httpClient: Client[F],
    val config: KubeConfig[F],
    val authorization: Option[F[Authorization]],
    private val apiVersion: SidecarsApi.ApiVersion = SidecarsApi.ApiVersion.V1,
)(implicit
    val F: Async[F],
    val listDecoder: Decoder[SidecarList],
    val resourceDecoder: Decoder[Sidecar],
    encoder: Encoder[Sidecar]
) extends Listable[F, SidecarList]
    with Watchable[F, Sidecar] {
  import SidecarsApi.*

  val resourceUri: Uri = baseUri(apiVersion) / "sidecars"

  def namespace(namespace: String): NamespacedSidecarsApi[F] =
    new NamespacedSidecarsApi(httpClient, config, authorization, namespace, apiVersion)
}

private[client] class NamespacedSidecarsApi[F[_]](
    val httpClient: Client[F],
    val config: KubeConfig[F],
    val authorization: Option[F[Authorization]],
    namespace: String,
    private val apiVersion: SidecarsApi.ApiVersion,
)(implicit
    val F: Async[F],
    val resourceEncoder: Encoder[Sidecar],
    val resourceDecoder: Decoder[Sidecar],
    val listDecoder: Decoder[SidecarList]
) extends Creatable[F, Sidecar]
    with Replaceable[F, Sidecar]
    with Gettable[F, Sidecar]
    with Listable[F, SidecarList]
    with Deletable[F]
    with DeletableTerminated[F]
    with GroupDeletable[F]
    with Watchable[F, Sidecar] {
  import SidecarsApi.*

  val resourceUri: Uri = baseUri(apiVersion) / "namespaces" / namespace / "sidecars"
}

