package org.scalabilityissues
import se.scalablesolutions.akka.actor.Actor
import se.scalablesolutions.akka.util.Logging
import org.scalatest.{GivenWhenThen, FeatureSpec}
import org.scalatest.matchers.ShouldMatchers

import Actor._
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
class WorkerPoolSpec extends FeatureSpec with GivenWhenThen with ShouldMatchers {
  feature("Workers can be registered and unregistered in the WorkerPool"){
    scenario("A Worker is registered, in an empty WorkerPool"){
      given("An Empty WorkerPool")
      val pool = actorOf[WorkerPoolImpl].start

      when("A Worker is registered")
      pool ! new Register(actorOf(new Worker("worker 1")).start)

      then("The Number of Workers is 1")
      pool !! HowManyWorkersRegistered should be(Some(1))
    }

    scenario("A Worker is registered, in the WorkerPool"){
      given("An WorkerPool with registered Workers")
      val pool = actorOf[WorkerPoolImpl].start
      for (i <- 1 to 3) pool ! new Register(actorOf(new Worker("worker "+i)).start)
      when("A Worker is registered")
      pool ! new Register(actorOf(new Worker("worker 4")).start)

      then("The Number of Workers is 4")
      pool !! HowManyWorkersRegistered should be(Some(4))
    }

    scenario("A Worker is unregistered, in the WorkerPool"){
      given("A WorkerPool with registered Workers")
      val pool = actorOf[WorkerPoolImpl].start
      val worker1  =actorOf(new Worker("worker 1")).start;
      pool ! new Register(worker1)
      pool ! new Register(actorOf(new Worker("worker 2")).start)
      pool ! new Register(actorOf(new Worker("worker 3")).start)

      when("A Worker is unregistered")
      pool ! new UnRegister(worker1)

      then("The Number of Workers is 1")
      pool !! HowManyWorkersRegistered should be(Some(2))
    }
  }
}

class WorkerPoolImpl extends Actor with WorkerPool{
  def receive = registrationManagement
}

class Worker(id : String) extends Actor{
  import self._
  self.id=id
  def receive = {
    case _ => log.info("Receive a message")
  }
}