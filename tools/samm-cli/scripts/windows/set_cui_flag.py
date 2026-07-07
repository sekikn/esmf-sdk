#!/usr/bin/env python3

import pefile
import shutil
import sys

from tempfile import NamedTemporaryFile

def set_subsystem_type(path, subsystem_type):
    f = NamedTemporaryFile(delete=False)
    with pefile.PE(path, fast_load=True) as pe:
      pe.OPTIONAL_HEADER.Subsystem = dict(pefile.subsystem_types)[subsystem_type]
      pe.write(f.name)
    f.close()
    shutil.move(f.name, path)

if __name__ == "__main__":
  set_subsystem_type(sys.argv[1], "IMAGE_SUBSYSTEM_WINDOWS_CUI")
