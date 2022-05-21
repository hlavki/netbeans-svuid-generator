package eu.hlavki.netbeans.svuid.service;

import javax.lang.model.element.TypeElement;

public interface SerialVersionUIDService {

    long generate(TypeElement typeElement);
}
