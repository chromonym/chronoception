for i in range(1,64):
    n = format(i,'02d')
    f = open(n+".json","x")
    f.write('''{
  "parent": "minecraft:item/generated",
  "textures": {
    "layer0": "chronoception:item/true_clock/'''+n+'''"
  }
}''')
    f.close()