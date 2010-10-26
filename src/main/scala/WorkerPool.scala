package org.scalabilityissues

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
case class Register(actor : ActorRef) extends Message
case class UnRegister (actor: ActorRef) extends Message
case class NewSession() extends Message
case class ReleaseSession(id : String) extends Message
case class Execute(id: String, cmd: Object)
case object HowManyWorkersRegistered
case object HowManyWorkersFree
case object HowManyWorkersBusy


trait WorkerPool { this: Actor =>
  import self._
  val workers = new HashMap[String,ActorRef]

  protected def registrationManagement : Receive = {
    case msg@Register(actor) => {
      log.debug("WorkerPool receive Register with "+actor)
      workers += (actor.id -> actor)
    }
    case msg@UnRegister(actor) => {
      log.debug("WorkerPool receive UnRegister with "+actor)
      workers -= actor.id
    }
    case HowManyWorkersRegistered => {
      log.debug("WorkerPool receive HowManyWorkersRegistered")
      reply(workers.size)
    }
  
  }
}