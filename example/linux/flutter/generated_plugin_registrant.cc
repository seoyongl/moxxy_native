//
//  Generated file. Do not edit.
//

// clang-format off

#include "generated_plugin_registrant.h"

#include <moxxy_native/moxxy_native_plugin.h>

void fl_register_plugins(FlPluginRegistry* registry) {
  g_autoptr(FlPluginRegistrar) moxxy_native_registrar =
      fl_plugin_registry_get_registrar_for_plugin(registry, "MoxxyNativePlugin");
  moxxy_native_plugin_register_with_registrar(moxxy_native_registrar);
}
