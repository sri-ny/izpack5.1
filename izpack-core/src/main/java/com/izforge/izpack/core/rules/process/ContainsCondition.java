package com.izforge.izpack.core.rules.process;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.adaptator.impl.XMLElementImpl;
import com.izforge.izpack.api.data.Variables;
import com.izforge.izpack.api.exception.CompilerException;
import com.izforge.izpack.api.rules.Condition;
import com.izforge.izpack.core.variable.utils.ValueUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ContainsCondition extends Condition {

  private static final long serialVersionUID = 114116957546161583L;

  private static final transient Logger logger = Logger
      .getLogger(ContainsCondition.class.getName());

  private static final String VALUE_ELEMENT_NAME = "value";
  private static final String VALUE_ATTR_REGEX_NAME = "regex";
  private static final String VALUE_ATTR_CASEINSENSITIVE_NAME = "caseInsensitive";

  private ContentType contentType;
  private String source = null;
  private String value = null;
  private boolean isRegEx = false;
  private boolean isCaseInsensitive = false;

  private Pattern pattern = null;

  public ContainsCondition() {
  }

  @Override
  public boolean isTrue()
  {

    if (this.source == null) {
        return false;
    }

    Variables variables = getInstallData().getVariables();
    String content = null;

    // resolve variables in <value>; 
    // must be done on each call again, because content could change 
    String resolvedValue = variables.replace(this.value);

    if (isRegEx)
    {
        pattern = Pattern.compile(resolvedValue,Pattern.MULTILINE);
    }

    switch (contentType) {
    case STRING:
        content = variables.replace(this.source);
        break;

    case VARIABLE:
        content = this.getInstallData().getVariable(this.source);
        break;

    case FILE:
        File file = new File(FilenameUtils.normalize(variables.replace(this.source)));
        byte[] buffer = new byte[(int) file.length()];
        BufferedInputStream f = null;
        try
        {
            f = new BufferedInputStream(new FileInputStream(file));
            f.read(buffer);
            if (f != null) try
            {
                f.close();
            }
            catch (IOException ignored)
            {}
        }
        catch (IOException e)
        {
            logger.log(Level.WARNING, e.getMessage());
            return false;
        }
        content = new String(buffer);
      break;

    default:
      logger.warning("Illegal source type '" + contentType.getAttribute()
          + "' for condition \"" + getId() + "\"");
      break;
    }

    if (content == null)
      return false;

    return matchesString(content, resolvedValue);
  }

  private boolean matchesString(String line, String value)
  {
      if (isRegEx)
      {
          Matcher matcher = pattern.matcher(line);
          if (matcher.find())
          {
              return true;
          }
      }
      else
      {
          if (isCaseInsensitive)
          {
              if (line.toLowerCase().contains(value.toLowerCase()))
              {
                  return true;
              }
          }
          else
          {
              if (line.contains(value))
              {
                  return true;
              }
          }

      }
      return false;
  }

  @Override
  public void readFromXML(IXMLElement xmlcondition)
      throws Exception
  {
    if (xmlcondition != null) {
      for (IXMLElement child : xmlcondition.getChildren()) {
        ContentType contentType = ContentType.getFromAttribute(child.getName());
        if (contentType != null) {
          this.contentType = contentType;
          if (source != null)
          {
            throw new Exception("Condition \"" + getId()
                + "\" has ambigous source elements");
          }
          this.source = child.getContent();
          if (this.source == null || this.source.length() == 0) {
            throw new Exception("Condition \"" + getId()
                + "\" has a nested '"+contentType.getAttribute()+"' element without valid contents");
          }
        } else {
          if (VALUE_ELEMENT_NAME.equalsIgnoreCase(child.getName()))
          {
            isRegEx = Boolean.valueOf(child.getAttribute(VALUE_ATTR_REGEX_NAME));
            isCaseInsensitive = Boolean.valueOf(child.getAttribute(VALUE_ATTR_CASEINSENSITIVE_NAME));
            this.value = child.getContent();
            if (this.value == null || this.value.length() == 0) {
              throw new Exception("Condition \"" + getId()
                  + "\" has a nested '"+VALUE_ELEMENT_NAME+"' element without valid contents");
            }
          }
          else
          {
            throw new Exception("Unknown nested element '" + child.getName()
                + "' for condition \"" + getId() + "\"");
          }
        }
      }
    }
  }

  public ContentType getContentType()
  {
    return contentType;
  }

  public void setContentType(ContentType contentType)
  {
    this.contentType = contentType;
  }

  public String getSource()
  {
    return source;
  }

  public void setSource(String content)
  {
    this.source = content;
  }

  @Override
  public void makeXMLData(IXMLElement conditionRoot)
  {
    XMLElementImpl el1 = new XMLElementImpl(this.contentType.getAttribute(),
        conditionRoot);
    el1.setContent(this.source);
    conditionRoot.addChild(el1);
    XMLElementImpl el2 = new XMLElementImpl(VALUE_ELEMENT_NAME, conditionRoot);
    el2.setContent(this.value);
    el2.setAttribute(VALUE_ATTR_REGEX_NAME, Boolean.toString(isRegEx));
    el2.setAttribute(VALUE_ATTR_CASEINSENSITIVE_NAME, Boolean.toString(isCaseInsensitive));
    conditionRoot.addChild(el2);
  }

  public enum ContentType {
    VARIABLE("variable"), STRING("string"), FILE("file");

    private static Map<String, ContentType> lookup;

    private String attribute;

    ContentType(String attribute) {
      this.attribute = attribute;
    }

    static {
      lookup = new HashMap<String, ContentType>();
      for (ContentType operation : EnumSet.allOf(ContentType.class)) {
        lookup.put(operation.getAttribute(), operation);
      }
    }

    public String getAttribute()
    {
      return attribute;
    }

    public static ContentType getFromAttribute(String attribute)
    {
      if (attribute != null && lookup.containsKey(attribute)) {
        return lookup.get(attribute);
      }
      return null;
    }
  }

    @Override
    public Set<String> getVarRefs() {
        HashSet<String> vars = new HashSet<String>(2);
        switch (contentType)
        {
            case VARIABLE:
                if (this.source != null)
                {
                    // variable is used in this case
                    vars.add(this.source);
                }
                break;
            case STRING:
            case FILE:
                if (this.source != null)
                {
                    // variables are resolved here
                    vars.addAll(ValueUtils.parseUnresolvedVariableNames(this.source));
                }
                break;
            default: throw new CompilerException("Unimplemented contentType");
        }
        return vars;
    }

}
