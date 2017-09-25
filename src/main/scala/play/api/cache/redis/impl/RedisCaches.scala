package play.api.cache.redis.impl

import javax.inject.Provider

import play.api.Environment
import play.api.cache.redis._
import play.api.inject.ApplicationLifecycle

import akka.actor.ActorSystem

/**
  * Aggregates all available redis APIs into a single handler. This simplifies
  * binding, construction, and accessing all APIs.
  *
  * @author Karel Cemus
  */
trait RedisCaches {
  def sync: CacheApi
  def async: CacheAsyncApi
  def scalaSync: play.api.cache.SyncCacheApi
  def javaSync: play.cache.SyncCacheApi
  def javaAsync: play.cache.AsyncCacheApi
}

class RedisCachesProvider( instance: RedisInstance, serializer: connector.AkkaSerializer, environment: Environment, recovery: RecoveryPolicyResolver )( implicit system: ActorSystem, lifecycle: ApplicationLifecycle ) extends Provider[ RedisCaches ] {
  private implicit lazy val runtime: RedisRuntime = RedisRuntime( instance, recovery( instance.recovery ) )( system )

  private lazy val redisConnector = new connector.RedisConnectorProvider( instance, serializer ).get

  lazy val get = new RedisCaches {
    lazy val async =  new AsyncRedis( redisConnector )
    lazy val sync = new SyncRedis( redisConnector )
    lazy val scalaSync = new play.api.cache.DefaultSyncCacheApi( async )
    lazy val java = new JavaRedis( async, environment )
    lazy val javaAsync = new play.cache.DefaultAsyncCacheApi( async )
    lazy val javaSync = new play.cache.DefaultSyncCacheApi( java )
  }
}
