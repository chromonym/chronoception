for i in range(1,16):
    n = format(i,'02d')
    f = open(n+".json","x")
    f.write('''{
  "parent": "minecraft:item/generated",
  "textures": {
    "layer0": "chronoception:item/stopwatch/'''+n+'''"
  }
}''')
    f.close()