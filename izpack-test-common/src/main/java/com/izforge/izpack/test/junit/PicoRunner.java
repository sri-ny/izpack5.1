/*
 * IzPack - Copyright 2001-2017 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.izforge.izpack.test.junit;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import com.izforge.izpack.api.container.Container;
import com.izforge.izpack.api.exception.IzPackException;

/**
 * Custom runner for getting dependencies injected in test with PicoContainer
 *
 * @author Anthonin Bonnefoy
 */
public class PicoRunner extends PlatformRunner
{
    private FrameworkMethod method;
    private Object currentTestInstance;
    private Class<? extends Container> containerClass;
    private Container containerInstance;

    /**
     * The logger.
     */
    private static final Logger logger = Logger.getLogger(PicoRunner.class.getName());

    /**
     * Creates a {@code PicoRunner} for the given test {@code klass}.
     *
     * @param klass The test class which is to be run.
     * @throws InitializationError If an initialization error occurs.
     */
    public PicoRunner(Class<?> klass) throws InitializationError
    {
        super(klass);
    }

    @Override
    protected void validateConstructor(List<Throwable> errors)
    {
    }

    @Override
    protected Statement methodBlock(FrameworkMethod method)
    {
        this.method = method;
        Statement statement = super.methodBlock(method);
        try
        {
            for (Field field : containerClass.getFields())
            {
                Annotation annotation = field.getAnnotation(Rule.class);
                if (annotation != null)
                {
                    TestRule rule = (TestRule) field.get(containerInstance);
                    Description description = Description.createTestDescription(
                            method.getMethod().getDeclaringClass(), method.getName());
                    statement = rule.apply(statement, description);

                }
            }
        }
        catch (IllegalAccessException e)
        {
            throw new IzPackException(e);
        }

        return statement;
    }

    /**
     * Creates an instance of the test class through a pico container.
     * <p>
     *     This is done by first creating an instance of the container specified
     *     by the {@link com.izforge.izpack.test.Container} annotation. The test
     *     class is then added as component to the container and finally retrieved
     *     through the container. This last step is run on the Event Dispatcher
     *     Thread.
     * </p>
     *
     * @return An instance of the test class.
     * @throws Exception If either the test container or test class could not be
     *      created or initialized.
     *
     * @see #createContainer(Class)
     */
    @Override
    protected Object createTest() throws Exception
    {
        final Class<?> javaClass = getTestClass().getJavaClass();

        logger.info("Creating test=" + getTestClass().getName());

        containerClass = javaClass.getAnnotation(com.izforge.izpack.test.Container.class).value();

        // create container outside of EDT which matches behaviour in InstallerGui
        containerInstance = createContainer(containerClass);
        containerInstance.addComponent(javaClass);

        SwingUtilities.invokeAndWait(new Runnable()
        {
            public void run()
            {
                try
                {
                    currentTestInstance = containerInstance.getComponent(javaClass);
                }
                catch (Exception e)
                {
                    logger.log(Level.SEVERE, e.getMessage(), e);
                    throw new IzPackException(e);
                }
            }
        });

        return currentTestInstance;
    }

    /**
     * Tries to create an instance from the given {@code containerClass}.
     * <p>
     *     There are a total of three constructor signatures which are recognized
     *     by this method:
     *     <ul>
     *         <li>Container(Class&lt;?&gt;, FrameworkMethod)</li>
     *         <li>Container(Class&lt;?&gt;)</li>
     *         <li>Container()</li>
     *     </ul>
     * </p>
     *
     * @param containerClass The container class which is to be instanced.
     * @return An instance of the given {@code containerClass} on success.
     * @throws Exception If the container class could not be instanced.
     */
    private Container createContainer(Class<? extends Container> containerClass)
            throws Exception
    {
        final Class<?> javaClass = getTestClass().getJavaClass();
        Constructor<? extends Container> constructor;
        Container result;

        try
        {
            constructor = containerClass.getConstructor(javaClass.getClass(), method.getClass());
            result = constructor.newInstance(javaClass, method);
        }
        catch (NoSuchMethodException exception)
        {
            try
            {
                constructor = containerClass.getConstructor(javaClass.getClass());
                result = constructor.newInstance(javaClass);
            }
            catch (NoSuchMethodException nested)
            {
                constructor = containerClass.getConstructor();
                result = constructor.newInstance();
            }
        }

        return result;
    }

}