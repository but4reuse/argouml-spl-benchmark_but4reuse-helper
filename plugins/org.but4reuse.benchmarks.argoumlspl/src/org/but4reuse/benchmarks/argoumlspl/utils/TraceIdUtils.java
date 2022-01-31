package org.but4reuse.benchmarks.argoumlspl.utils;

import java.util.List;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

/**
 * Trace Id utils
 * 
 * @author jabier.martinez
 */
public class TraceIdUtils {

	/**
	 * Get id of a package
	 * 
	 * @param packageDeclaration
	 * @return id of the package
	 */
	public static String getId(PackageDeclaration packageDeclaration) {
		if (packageDeclaration == null) {
			return "defaultPackage";
		}
		return packageDeclaration.getName().getFullyQualifiedName();
	}

	/**
	 * Get id of a type
	 * 
	 * @param typeDeclaration
	 * @return id of the type
	 */
	public static String getId(TypeDeclaration typeDeclaration) {
		StringBuffer qname = new StringBuffer();
		CompilationUnit cu = (CompilationUnit) typeDeclaration.getRoot();
		qname.append(getId(cu.getPackage()));
		qname.append(".");
		// nested and inner classes
		StringBuffer parentChain = new StringBuffer();
		Object parent = typeDeclaration.getParent();
		while (parent != null && parent instanceof TypeDeclaration) {
			parentChain.insert(0, ((TypeDeclaration) parent).getName().getFullyQualifiedName() + ".");
			parent = ((TypeDeclaration) parent).getParent();
		}
		qname.append(parentChain);
		qname.append(typeDeclaration.getName().getFullyQualifiedName());
		return qname.toString();
	}

	/**
	 * Get id of a method
	 * 
	 * @param methodDeclaration
	 * @return id of the method
	 */
	public static String getId(MethodDeclaration methodDeclaration) {
		StringBuffer qname = new StringBuffer();
		// TODO handle AnonymousClassDeclaration
		if (methodDeclaration.getParent() instanceof TypeDeclaration) {
			TypeDeclaration typeParent = (TypeDeclaration) methodDeclaration.getParent();
			qname.append(getId(typeParent));
			qname.append(" ");
			qname.append(methodDeclaration.getName().getIdentifier());
			// add parameters to signature
			qname.append("(");
			List<?> parameters = methodDeclaration.parameters();
			for (Object parameter : parameters) {
				if (parameter instanceof SingleVariableDeclaration) {
					qname.append(((SingleVariableDeclaration) parameter).getType());
					qname.append(",");
				}
			}
			// remove last comma
			if (!parameters.isEmpty()) {
				qname.setLength(qname.length() - 1);
			}
			qname.append(")");
		} else {
			System.err.println(
					"TraceIdUtils: Method in " + methodDeclaration.getParent().getClass().getSimpleName() + " not handled");
		}
		return qname.toString();
	}
}