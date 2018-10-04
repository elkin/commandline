package io.github.elkin.commandline;

import io.github.elkin.commandline.exception.ValidationException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class GroupValidator implements Validator {
    private int d_groupId;
    private final Set<Integer> d_activeGroupId;
    private final List<Group> d_groups;

    public class Group {
        private final String d_name;
        private final int d_id;
        private final List<Option> d_options;
        private final List<Flag> d_flags;

        private Group(int id)
        {
            this("", id);
        }

        private Group(String name, int id)
        {
            assert name != null;
            assert id >= 0;

            d_name = name;
            d_id = id;
            d_options = new ArrayList<>();
            d_flags = new ArrayList<>();
        }

        public Group addFlag(Flag flag)
        {
            Objects.requireNonNull(flag);
            flag.setConsumer(
                    flag.consumer().andThen(f -> {
                        d_activeGroupId.add(d_id);
                        d_flags.add(flag);
                    }));
            return this;
        }

        public Group addFlags(Flag... flags)
        {
            Objects.requireNonNull(flags);

            for (Flag flag : flags) {
                addFlag(flag);
            }
            return this;
        }

        public Group addOption(Option option)
        {
            Objects.requireNonNull(option);

            option.setConsumer(
                    option.consumer().andThen(o -> {
                        d_activeGroupId.add(d_id);
                        d_options.add(option);
                    }));
            return this;
        }

        public Group addOptions(Option... options)
        {
            Objects.requireNonNull(options);

            for (Option option : options) {
                addOption(option);
            }
            return this;
        }

        @Override
        public String toString()
        {
            StringBuilder result = new StringBuilder("Group ");
            if (d_name.isEmpty()) {
                result.append(d_id);
            } else {
                result.append(d_name);
            }
            result.append(' ');

            for (Iterator<Option> iter = d_options.iterator(); iter.hasNext();) {
                Option option = iter.next();
                String description = String.join("|", option.prefixes());
                result.append(description);
                if (iter.hasNext()) {
                    result.append(", ");
                }
            }

            if (!d_flags.isEmpty()) {
                result.append(", ");
            }

            for (Iterator<Flag> iter = d_flags.iterator(); iter.hasNext();) {
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


    public GroupValidator()
    {
        d_activeGroupId = new HashSet<>();
        // d_groupId = 0; not needed
        d_groups = new ArrayList<>();
    }

    public Group addGroup()
    {
        return addGroup("");
    }

    public Group addGroup(String name)
    {
        Objects.requireNonNull(name);
        Group group = new Group(name, d_groupId++);
        d_groups.add(group);
        return group;
    }

    @Override
    public void validate(List<Argument> arguments, List<Option> options, List<Flag> flags)
    {
        assert arguments != null;
        assert options != null;
        assert flags != null;

        if (d_activeGroupId.size() > 1) {
            StringBuilder result = new StringBuilder(
                    "Options/flags from different groups can't be used together:\n");
            for (Integer groupId : d_activeGroupId) {
                Group group = d_groups.get(groupId);
                result.append(group);
                result.append('\n');
            }
            throw new ValidationException(result.toString());
        }
    }
}
