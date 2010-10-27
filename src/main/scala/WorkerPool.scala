package org.scalabilityissues

import java.util.UUID
import se.scalablesolutions.akka.actor.{ActorRef, Actor}
import se.scalablesolutions.akka.util.Logging
import collection.mutable.HashMap

/*
 * This software is licensed under the Apache 2 license, quoted below.
 *  
 *  
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *  
 *   http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

sealed class Message
case class Register(actor: ActorRef) extends Message
case class UnRegister(actor: ActorRef) extends Message
case class NewSession() extends Message
case class ReleaseSession(id: String) extends Message
case class Execute(id: String, cmd: Object)
case object HowManyWorkersRegistered
case object HowManyWorkersFree
case object HowManyWorkersReserved
case object NoFreeWorkerAvailable

case class WorkerDescription(worker: ActorRef, var sessionId : Option[String])

trait WorkerPool {
  this: Actor =>
  import self._

  /**
   * HashMap[actorId -> ActorRef]
   */
  val workers = new HashMap[String, WorkerDescription]

  /**
   *  Register new workers,
   * and unregister known workers.
   *
   * New workers are set as free.
   * Busy as free workers can be unregistered.
   */
  protected def registrationManagement: Receive = {
    case msg@Register(actor) => {
      log.debug("WorkerPool receive Register with " + actor)
      workers += (actor.id -> new WorkerDescription(actor,None))
    }
    case msg@UnRegister(actor) => {
      log.debug("WorkerPool receive UnRegister with " + actor)
      workers -= actor.id
    }
    case HowManyWorkersRegistered => {
      log.debug("WorkerPool receive HowManyWorkersRegistered")
      reply(workers.size)
    }
  }


  /**
   * Allocate a free worker to a new session, the worker become reserved.
   *
   * Or release a session in order to free a reserved worker.
   *
   * The sessionId is used to execute commands on a reserved worker.
   */
  protected def sessionManagement: Receive = {
    case msg@NewSession() => {
      log.debug("WorkerPool receive NewSession")
      workers.values.find { worker => !worker.sessionId.isDefined } match {
        case None => {
          log.info("No free worker avalaible.")
          reply(NoFreeWorkerAvailable)
        }
        case Some(freeWorker : WorkerDescription) => {
          val id = UUID.randomUUID.toString
          freeWorker.sessionId = Some(id)
          reply(id)
        }
      }

    }
    case msg@ReleaseSession(id) => {
      log.debug("WorkerPool receive ReleaseSession")
      workers.values.find { worker => worker.sessionId == Some(id) } match {
        case None => {
          log.warning("Worker can not be release, because not found, with id : " + id)

        }
        case Some(freeWorker : WorkerDescription) => {
           freeWorker.sessionId = None
        }
      }

    }
    case msg@Execute(id, cmd) => {
      log.debug("WorkerPool receive Execute")
    }
    case HowManyWorkersFree => {
      log.debug("WorkerPool receive HowManyWorkersFree")
      reply (workers.values.filter { worker => !worker.sessionId.isDefined }.size)
    }
    case HowManyWorkersReserved => {
      log.debug("WorkerPool receive HowManyWorkersReserved")
      reply (workers.values.filter { worker => worker.sessionId.isDefined }.size)
    }
  }
}