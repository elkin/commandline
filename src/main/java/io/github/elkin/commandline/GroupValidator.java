package io.github.elkin.commandline;

import io.github.elkin.commandline.exception.ValidationException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class GroupValidator implements Validator {

  private final Set<Integer> activeGroupIds;
  private final List<Group> groups;
  private int groupId;

  public GroupValidator() {
    activeGroupIds = new HashSet<>();
    // groupId = 0; not needed, by default
    groups = new ArrayList<>();
  }

  public Group addGroup() {
    return addGroup("");
  }

  public Group addGroup(String name) {
    Objects.requireNonNull(name);
    Group group = new Group(name, groupId++);
    groups.add(group);
    return group;
  }

  @Override
  public void validate(List<Argument> arguments, List<Option> options, List<Flag> flags) {
    assert arguments != null;
    assert options != null;
    assert flags != null;

    if (activeGroupIds.size() > 1) {
      StringBuilder result = new StringBuilder(
          "Options/flags from different groups can't be used together:")
          .append(System.lineSeparator());
      for (Integer gid : activeGroupIds) {
        Group group = groups.get(gid);
        result.append(group);
        result.append(System.lineSeparator());
      }
      throw new ValidationException(result.toString());
    }
  }

  public class Group {

    private final String name;
    private final int id;
    private final List<Option> options;
    private final List<Flag> flags;

    private Group(String name, int id) {
      assert name != null;
      assert id >= 0;

      this.name = name;
      this.id = id;
      options = new ArrayList<>();
      flags = new ArrayList<>();
    }

    public Group addFlag(Flag flag) {
      Objects.requireNonNull(flag);
      flag.setConsumer(
          flag.consumer().andThen(f -> {
            activeGroupIds.add(id);
            flags.add(flag);
          }));
      return this;
    }

    public Group addFlags(Flag... flags) {
      Objects.requireNonNull(flags);

      for (Flag flag : flags) {
        addFlag(flag);
      }
      return this;
    }

    public Group addOption(Option option) {
      Objects.requireNonNull(option);

      option.setConsumer(
          option.consumer().andThen(o -> {
            activeGroupIds.add(id);
            options.add(option);
          }));
      return this;
    }

    public Group addOptions(Option... options) {
      Objects.requireNonNull(options);

      for (Option option : options) {
        addOption(option);
      }
      return this;
    }

    @Override
    public String toString() {
      StringBuilder result = new StringBuilder("Group ");
      if (name.isEmpty()) {
        result.append(id);
      } else {
        result.append(name);
      }
      result.append(' ');

      for (Iterator<Option> iter = options.iterator(); iter.hasNext(); ) {
        Option option = iter.next();
        String description = String.join("|", option.prefixes());
        result.append(description);
        if (iter.hasNext()) {
          result.append(", ");
        }
      }

      if (!flags.isEmpty()) {
        result.append(", ");
      }

      for (Iterator<Flag> iter = flags.iterator(); iter.hasNext(); ) {
        Flag flag = iter.next();
        String description = String.join("|", flag.prefixes());
        result.append(description);
        if (iter.hasNext()) {
          result.append(", ");
        }
      }

      return result.toString();
    }
  }
}
