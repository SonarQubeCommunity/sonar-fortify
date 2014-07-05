/*
 * Fortify Plugin for SonarQube
 * Copyright (C) 2014 Vivien HENRIET and SonarSource
 * dev@sonar.codehaus.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.fortify.rule.handler;

import org.sonar.fortify.base.handler.AbstractHandler;
import org.sonar.fortify.base.handler.StringHandler;
import org.sonar.fortify.rule.element.RulePack;
import org.sonar.fortify.rule.element.Rules;

public class RulePackHandler extends AbstractHandler<RulePack> {
  private final StringHandler nameHandler;
  private final StringHandler languageHandler;
  private final RulesHandler rulesHandler;

  public RulePackHandler() {
    super("RulePack");
    this.nameHandler = new StringHandler("Name");
    this.languageHandler = new StringHandler("Language");
    this.rulesHandler = new RulesHandler();
    setChildren(this.nameHandler, this.languageHandler, this.rulesHandler);
  }

  @Override
  public void end() {
    RulePack rulePack = new RulePack();
    rulePack.setName(this.nameHandler.getResult());
    rulePack.setLanguage(this.languageHandler.getResult());
    Rules rules = this.rulesHandler.getResult();
    if (rules != null) {
      rulePack.addDescriptions(rules.getDescriptions());
      rulePack.addRules(rules.getRules());
    }
    setResult(rulePack);
  }
}
