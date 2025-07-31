package org.bdch

import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Specification

class AbstractInterfaceControllerSpec extends Specification implements ControllerUnitTest<AbstractInterfaceController> {

    def setup() {
    }

    def cleanup() {
    }

    void "test something"() {
        expect:"fix me"
            true == false
    }
}
