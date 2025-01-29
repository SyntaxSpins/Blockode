package dev.trindadedev.blockode.editor.generator;

import static java.lang.String.format;

import dev.trindadedev.blockode.beans.BlockBean;
import dev.trindadedev.blockode.utils.BlockUtil;
import java.util.List;

public class JavaGenerator {

  public static String generate(final List<BlockBean> blocks) {
    var code = new StringBuilder();
    blocks.forEach(
        it -> {
          code.append(getBlockCode(it));
        });
    return code.toString();
  }

  public static String getBlockCode(final BlockBean bean) {
    var toReturnCode = "";
    var params = bean.parameters;
    toReturnCode =
        switch (bean.opCode) {
          case BlockUtil.BLOCK_OPCODE_DEFINED_FUNC -> null;
          case BlockUtil.BLOCK_OPCODE_GET_VAR -> bean.spec;
          case BlockUtil.BLOCK_OPCODE_SET_VAR_BOOL,
              BlockUtil.BLOCK_OPCODE_SET_VAR_STR,
              BlockUtil.BLOCK_OPCODE_SET_VAR_INT -> format(
              "%s = %s;", params.get(0), params.get(1));
          case BlockUtil.BLOCK_OPCODE_DO_PRINT -> format("System.out.printLn(%s);", params.get(0));
          case BlockUtil.BLOCK_OPCODE_ADD_SOURCE_DIRECTLY -> params.get(0);
          default -> "Not possible to generate";
        };
    return toReturnCode;
  }
}
