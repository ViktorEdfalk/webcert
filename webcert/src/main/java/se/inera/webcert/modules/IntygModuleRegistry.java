package se.inera.webcert.modules;

import java.util.List;

import se.inera.certificate.modules.support.api.ModuleApi;

public interface IntygModuleRegistry {

    ModuleApi getModuleApi(String id);

    IntygModule getIntygModule(String id);

    List<IntygModule> listAllModules();

}
