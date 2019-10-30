package com.scottlogic.deg.profile.commands;

import com.scottlogic.deg.common.commands.CommandBase;
import com.scottlogic.deg.common.profile.ProfileFields;
import com.scottlogic.deg.generator.profile.Rule;

import java.util.Optional;

public class CreateSpecificTypesRule extends CommandBase<Optional<Rule>>
{
    public final ProfileFields fields;

    public CreateSpecificTypesRule(ProfileFields fields)
    {
        this.fields = fields;
    }
}
