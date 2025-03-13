ui_print "- Riru Enabled"
rm -rf "$MODPATH/zygisk" || true
ui_print "- Extracting extra libraries"
set_perm_recursive "$MODPATH" 0 0 0755 0644
rm -f "/data/local/tmp/lib$RIRU_MODULE_LIB_NAME.debug.dex" > /dev/null 2>&1 || true
rm -f "/data/local/tmp/lib$RIRU_MODULE_LIB_NAME.dex" > /dev/null 2>&1 || true