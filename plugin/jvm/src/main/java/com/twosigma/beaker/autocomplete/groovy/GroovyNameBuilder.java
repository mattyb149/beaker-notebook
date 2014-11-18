/*
 *  Copyright 2014 TWO SIGMA OPEN SOURCE, LLC
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.twosigma.beaker.autocomplete.groovy;

import org.antlr.v4.runtime.tree.ParseTree;

import com.twosigma.beaker.autocomplete.AutocompleteCandidate;
import com.twosigma.beaker.autocomplete.AutocompleteRegistry;
import com.twosigma.beaker.autocomplete.ClassUtils;
import com.twosigma.beaker.autocomplete.groovy.GroovyParser.AssignmentExpressionContext;
import com.twosigma.beaker.autocomplete.groovy.GroovyParser.ClassNameExpressionContext;
import com.twosigma.beaker.autocomplete.groovy.GroovyParser.DeclarationRuleContext;
import com.twosigma.beaker.autocomplete.groovy.GroovyParser.ExpressionContext;
import com.twosigma.beaker.autocomplete.groovy.GroovyParser.ListConstructorContext;
import com.twosigma.beaker.autocomplete.groovy.GroovyParser.MapConstructorContext;
import com.twosigma.beaker.autocomplete.groovy.GroovyParser.NewInstanceRuleContext;
import com.twosigma.beaker.autocomplete.groovy.GroovyParser.PathExpressionContext;
import com.twosigma.beaker.autocomplete.groovy.GroovyParser.TypeDeclarationContext;

public class GroovyNameBuilder extends GroovyAbstractListener{
  private AutocompleteRegistry registry;
  private ClassUtils classUtils;
  
  public GroovyNameBuilder(AutocompleteRegistry r, ClassUtils cu) {
      registry = r;
      classUtils = cu;
  }
  
  @Override
  public void exitDeclarationRule(DeclarationRuleContext ctx) {
    if(ctx.getChildCount()==4 &&
        ctx.getChild(0) instanceof TypeDeclarationContext &&
        ctx.getChild(3) instanceof ExpressionContext &&
        ctx.getChild(3).getChildCount()>0) {
      if(!ctx.getChild(1).getText().contains(".")) {
        if(ctx.getChild(3).getChild(0) instanceof PathExpressionContext) {
          String typpen = ctx.getChild(3).getChild(0).getText().trim();
          AutocompleteCandidate c = new AutocompleteCandidate(GroovyCompletionTypes.NAME, ctx.getChild(1).getText());
          registry.addCandidate(c);
          if(GroovyCompletionTypes.debug) System.out.println("define variable of type "+ctx.getChild(1).getText()+" "+typpen);
          if(classUtils.getVariableType(typpen)!=null) {
            classUtils.defineVariable(ctx.getChild(1).getText(), classUtils.getVariableType(typpen));
          }
        } else if(ctx.getChild(3).getChild(0) instanceof NewInstanceRuleContext) {
          ParseTree t = findChildrenByType(ctx.getChild(3).getChild(0),ClassNameExpressionContext.class);
          if(t!=null) {
            String ttype = t.getText().trim();
            AutocompleteCandidate c = new AutocompleteCandidate(GroovyCompletionTypes.NAME, ctx.getChild(1).getText());
            registry.addCandidate(c);
            if(GroovyCompletionTypes.debug) System.out.println("define variable of type "+ctx.getChild(1).getText()+" "+ttype);
            if(ttype!=null)
              classUtils.defineVariable(ctx.getChild(1).getText(), ttype);
          }
        } else {
          if(GroovyCompletionTypes.debug) System.out.println(((ExpressionContext)ctx.getChild(3)).getStart().getType());

          String typpen = null;
          if(ctx.getChild(3) instanceof ListConstructorContext)
            typpen="Array";
          else if(ctx.getChild(3) instanceof MapConstructorContext)
            typpen="Map";
          else {
            switch(((ExpressionContext)ctx.getChild(3)).getStart().getType()) {
            case GroovyLexer.STRING: typpen="String"; break;
            case GroovyLexer.INTEGER: typpen="Integer"; break;
            case GroovyLexer.DECIMAL: typpen="Double"; break;
            }
          }
          AutocompleteCandidate c = new AutocompleteCandidate(GroovyCompletionTypes.NAME, ctx.getChild(1).getText());
          registry.addCandidate(c);
          if(GroovyCompletionTypes.debug) System.out.println("define variable of type "+ctx.getChild(1).getText()+" "+typpen);
          if(typpen!=null)
            classUtils.defineVariable(ctx.getChild(1).getText(), typpen);
        }
      }
    }
  }

  @Override
  public void exitAssignmentExpression(AssignmentExpressionContext ctx) {
    if(ctx.getChildCount()==3 &&
        ctx.getChild(1).getText().equals("=") &&
        !ctx.getChild(0).getText().contains(".")) {
      
      if(ctx.getChild(2).getChild(0) instanceof PathExpressionContext) {
        String typpen = ctx.getChild(2).getChild(0).getText().trim();
        AutocompleteCandidate c = new AutocompleteCandidate(GroovyCompletionTypes.NAME, ctx.getChild(0).getText());
        registry.addCandidate(c);
        if(GroovyCompletionTypes.debug) System.out.println("define variable of type "+ctx.getChild(0).getText()+" "+typpen);
        if(classUtils.getVariableType(typpen)!=null) {
            classUtils.defineVariable(ctx.getChild(0).getText(), classUtils.getVariableType(typpen));
        }
      } else if(ctx.getChild(2).getChild(0) instanceof NewInstanceRuleContext) {
        ParseTree t = findChildrenByType(ctx.getChild(2).getChild(0),ClassNameExpressionContext.class);
        if(t!=null) {
          String ttype = t.getText().trim();
          AutocompleteCandidate c = new AutocompleteCandidate(GroovyCompletionTypes.NAME, ctx.getChild(0).getText());
          registry.addCandidate(c);
          if(GroovyCompletionTypes.debug) System.out.println("define variable of type "+ctx.getChild(0).getText()+" "+ttype);
          if(ttype!=null)
            classUtils.defineVariable(ctx.getChild(0).getText(), ttype);
        }
      } else {
        if(GroovyCompletionTypes.debug) System.out.println(((ExpressionContext)ctx.getChild(2)).getStart().getType());
        
        String typpen = null;
        switch(((ExpressionContext)ctx.getChild(2)).getStart().getType()) {
        case GroovyLexer.STRING: typpen="String"; break;
        case GroovyLexer.INTEGER: typpen="Integer"; break;
        case GroovyLexer.DECIMAL: typpen="Double"; break;
        }
        AutocompleteCandidate c = new AutocompleteCandidate(GroovyCompletionTypes.NAME, ctx.getChild(0).getText());
        registry.addCandidate(c);
        if(GroovyCompletionTypes.debug) System.out.println("define variable of type "+ctx.getChild(0).getText()+" "+typpen);
        if(typpen!=null)
          classUtils.defineVariable(ctx.getChild(0).getText(), typpen);
      }
      
    }

  }
  
  private ParseTree findChildrenByType(ParseTree parseTree, Class<?> classtype) {
    for(int i=0; i<parseTree.getChildCount(); i++) {
      ParseTree chl = parseTree.getChild(i);
      if(chl.getClass().equals(classtype)) {
        return chl;
      }
    }
    return null;
  }

  
}
