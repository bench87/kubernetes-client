package com.goyeau.kubernetes.client.api

import cats.effect.Async
import com.goyeau.istio.models.virtualService.*
import com.goyeau.kubernetes.client.KubeConfig
import com.goyeau.kubernetes.client.operation.*
import io.circe.*
import org.http4s.Uri
import org.http4s.client.Client
import org.http4s.headers.Authorization
import org.http4s.implicits.*

private[client] object VirtualServicesApi {
  enum ApiVersion(val segment: String) {
    case V1 extends ApiVersion("v1")
    case V1Beta1 extends ApiVersion("v1beta1")
  }

  private[client] def baseUri(version: ApiVersion): Uri =
    uri"/apis" / "networking.istio.io" / version.segment
}

private[client] class VirtualServicesApi[F[_]](
    val httpClient: Client[F],
    val config: KubeConfig[F],
    val authorization: Option[F[Authorization]],
    private val apiVersion: VirtualServicesApi.ApiVersion = VirtualServicesApi.ApiVersion.V1
)(implicit
    val F: Async[F],
    val listDecoder: Decoder[VirtualServiceList],
    val resourceDecoder: Decoder[VirtualService],
    encoder: Encoder[VirtualService]
) extends Listable[F, VirtualServiceList]
    with Watchable[F, VirtualService] {
  import VirtualServicesApi.*

  val resourceUri: Uri = baseUri(apiVersion) / "virtualservices"

  def namespace(namespace: String): NamespacedVirtualServicesApi[F] =
    new NamespacedVirtualServicesApi(httpClient, config, authorization, namespace, apiVersion)
}

private[client] class NamespacedVirtualServicesApi[F[_]](
    val httpClient: Client[F],
    val config: KubeConfig[F],
    val authorization: Option[F[Authorization]],
    namespace: String,
    private val apiVersion: VirtualServicesApi.ApiVersion
)(implicit
    val F: Async[F],
    val resourceEncoder: Encoder[VirtualService],
    val resourceDecoder: Decoder[VirtualService],
    val listDecoder: Decoder[VirtualServiceList]
) extends Creatable[F, VirtualService]
    with Replaceable[F, VirtualService]
    with Gettable[F, VirtualService]
    with Listable[F, VirtualServiceList]
    with Deletable[F]
    with DeletableTerminated[F]
    with GroupDeletable[F]
    with Watchable[F, VirtualService] {
  import VirtualServicesApi.*

  val resourceUri: Uri = baseUri(apiVersion) / "namespaces" / namespace / "virtualservices"
}

