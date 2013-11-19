package se.inera.webcert.spec

import groovyx.net.http.RESTClient
import se.inera.webcert.spec.util.RestClientFixture

/**
 *
 * @author andreaskaltenbach
 */
class LoggMeddelande extends RestClientFixture {

    def logs
    def counter = -1

    def beginTable() {
        def restClient = new RESTClient(logSenderBaseUrl)
        logs = restClient.get(path: "loggtjanst-stub").data
    }

    def execute() {
        counter++
    }

    def systemId() { logs.get(counter).system.systemId}
    def activityType() { logs.get(counter).activity.activityType}
    def purpose() { logs.get(counter).activity.purpose}
    def userId() { logs.get(counter).user.userId}
    def careProvider() { logs.get(counter).user.careProvider.careProviderId}
    def careUnit() { logs.get(counter).user.careUnit.careUnitId}
    def resourceType() { logs.get(counter).resources.resource.get(0).resourceType}

    def finns() {
        return fragaSvar != null
    }

    def internId() {
        fragaSvar.internReferens
    }

    def fraga() {
        fragaSvar.frageText
    }

    def svar() {
        fragaSvar.svarsText
    }

    def rensa() {
        def restClient = new RESTClient(logSenderBaseUrl)
        restClient.delete(path: "loggtjanst-stub")
    }
}

