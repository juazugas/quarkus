package io.quarkus.arc.test.buildextension.injectionPoints;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.inject.Qualifier;

import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.arc.Arc;
import io.quarkus.arc.ArcContainer;
import io.quarkus.arc.processor.InjectionPointsTransformer;
import io.quarkus.arc.test.ArcTestContainer;
import io.quarkus.arc.test.MyQualifier;

public class InjectionPointTransformerTest {

    @RegisterExtension
    public ArcTestContainer container = ArcTestContainer.builder()
            .beanClasses(SimpleProducer.class, SimpleConsumer.class, MyQualifier.class, CasualObserver.class,
                    AnotherQualifier.class)
            .injectionPointsTransformers(new MyTransformer())
            .build();

    @Test
    public void testQualifierWasAddedToInjectionPoint() {
        ArcContainer arc = Arc.container();
        assertTrue(arc.instance(SimpleConsumer.class).isAvailable());
        SimpleConsumer bean = arc.instance(SimpleConsumer.class).get();
        assertEquals("foo", bean.getFoo());
        assertEquals("bar", bean.getBar());

        assertTrue(arc.instance(CasualObserver.class).isAvailable());
        Arc.container().beanManager().getEvent().select(Integer.class).fire(42);
        CasualObserver observer = arc.instance(CasualObserver.class).get();
        assertEquals("bar", observer.getChangedString());
        assertEquals("foo", observer.getChanged2String());
        assertEquals("foo", observer.getUnchangedString());
    }

    @ApplicationScoped
    static class SimpleProducer {

        @Produces
        @MyQualifier
        @Dependent
        String producedString = "foo";

        @Produces
        @Dependent
        String producedStringNoQualifier = "bar";

    }

    @ApplicationScoped
    static class SimpleConsumer {

        @Inject
        // We will add @MyQualifier here
        String foo;

        @Inject
        // Nothing should be added here
        String bar;

        public String getBar() {
            return bar;
        }

        public String getFoo() {
            return foo;
        }
    }

    @Qualifier
    @Inherited
    @Target({ TYPE, METHOD, FIELD, PARAMETER })
    @Retention(RUNTIME)
    @interface AnotherQualifier {

    }

    @ApplicationScoped
    static class CasualObserver {

        String changeMe;
        String changeMe2;
        String dontChangeMe;

        // there is no String with qualifier @AnotherQualifier, we will remove it
        public void observe(@Observes Integer payload, @AnotherQualifier String changeMe, @MyQualifier String dontChangeMe,
                String changeMe2) {
            this.changeMe = changeMe;
            this.changeMe2 = changeMe2;
            this.dontChangeMe = dontChangeMe;
        }

        public String getChangedString() {
            return changeMe;
        }

        public String getChanged2String() {
            return changeMe2;
        }

        public String getUnchangedString() {
            return dontChangeMe;
        }
    }

    static class MyTransformer implements InjectionPointsTransformer {

        @Override
        public boolean appliesTo(Type requiredType) {
            // applies to all String injection points
            return requiredType.equals(Type.create(DotName.createSimple(String.class.getName()), Type.Kind.CLASS));
        }

        @Override
        public void transform(TransformationContext transformationContext) {
            AnnotationTarget.Kind kind = transformationContext.getAnnotationTarget().kind();
            if (AnnotationTarget.Kind.FIELD.equals(kind)) {
                FieldInfo fieldInfo = transformationContext.getAnnotationTarget().asField();
                // with this we should be able to filter out only fields we want to affect
                if (fieldInfo.declaringClass().name().equals(DotName.createSimple(SimpleConsumer.class.getName()))
                        && fieldInfo.name().equals("foo")) {
                    transformationContext.transform().add(MyQualifier.class).done();
                }

            } else if (AnnotationTarget.Kind.METHOD_PARAMETER.equals(kind)) {
                MethodInfo methodInfo = transformationContext.getAnnotationTarget().asMethodParameter().method();
                DotName anotherQualifierDotName = DotName.createSimple(AnotherQualifier.class.getName());
                if (methodInfo.declaringClass().name()
                        .equals(DotName.createSimple(CasualObserver.class.getName()))) {
                    if (transformationContext.getAllTargetAnnotations().stream()
                            .anyMatch(p -> p.name().equals(anotherQualifierDotName))) {
                        transformationContext.transform()
                                .remove(annotationInstance -> annotationInstance.name().equals(anotherQualifierDotName))
                                .done();
                    } else if (transformationContext.getAllTargetAnnotations().isEmpty()) {
                        transformationContext.transform().add(MyQualifier.class).done();
                    }

                }
            } else {
                throw new IllegalStateException("Unexpected injection point kind: " + kind);
            }
        }
    }
}
