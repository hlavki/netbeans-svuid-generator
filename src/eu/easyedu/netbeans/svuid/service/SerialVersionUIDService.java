/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.easyedu.netbeans.svuid.service;

import javax.lang.model.element.TypeElement;

/**
 *
 * @author hlavki
 */
public interface SerialVersionUIDService {

    long generate(TypeElement typeElement);
}
