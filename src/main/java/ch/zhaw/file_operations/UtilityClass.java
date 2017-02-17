package ch.zhaw.file_operations;

import japa.parser.ASTHelper;
import japa.parser.ASTParserConstants;
import japa.parser.ASTParserTokenManager;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.ImportDeclaration;
import japa.parser.ast.Node;
import japa.parser.ast.PackageDeclaration;
import japa.parser.ast.body.*;
import japa.parser.ast.expr.*;
import japa.parser.ast.stmt.BlockStmt;
import japa.parser.ast.type.ClassOrInterfaceType;
import japa.parser.ast.type.Type;
import japa.parser.ast.visitor.GenericVisitor;
import japa.parser.ast.visitor.VoidVisitor;
import japa.parser.ast.visitor.VoidVisitorAdapter;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class UtilityClass {

    /**
     * Writes Compilation unit object into the file at certain path
     * @param path String to save object
     * @param cu Compilation unit object to save
     */
    public static void writeCuToFile(String path, CompilationUnit cu) {
        try {
            PrintWriter writer = new PrintWriter(path, "UTF-8");
            writer.print(cu);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Replace the first letter of input string to the same uppercase letter
     * @param string is input String to translation
     * @return input string with first letter to upper case
     */
    public static String firstLetterToUpperCase(String string) {
        String first = string.substring(0, 1);
        String second = string.substring(1, string.length());
        first = first.toUpperCase();
        return first + second;
    }

    /**
     * Checks the accessibility of the field from certain method
     * @param methodDeclaration is method from where checks the access to field
     * @param fieldDeclaration is field to access from method
     * @return true if the  @param fieldDeclaration is accessible from the @param methodDeclaration
     */
    public static boolean isFieldAccessible(MethodDeclaration methodDeclaration, FieldDeclaration fieldDeclaration) {
        boolean result = true;
        if (ModifierSet.isStatic(methodDeclaration.getModifiers()) & !ModifierSet.isStatic(fieldDeclaration.getModifiers())) {
            result = false;
        }
        return result;
    }

    /**
     * Creates input class for Lambda function based on the method
     * @param methodEntity is a basic method for Lambda function and creating the input class
     * @return Compilation unit object of the created class
     */
    private static CompilationUnit createInPutType(MethodEntity methodEntity) {
        List<Parameter> parameters = methodEntity.getMethodDeclaration().getParameters();
        CompilationUnit inputCu = new CompilationUnit();
        ClassOrInterfaceDeclaration declaration =
                new ClassOrInterfaceDeclaration(ModifierSet.PUBLIC, false, "InputType");
        List<FieldDeclaration> fields = methodEntity.getClassEntity().getFields();
        ASTHelper.addTypeDeclaration(inputCu, declaration);
        ArrayList<String> fieldsNames = new ArrayList<>();
        if (fields != null) {
            for (FieldDeclaration field :
                    fields) {
                if (isFieldAccessible(methodEntity.getMethodDeclaration(), field)) {
                    for (VariableDeclarator var :
                            field.getVariables()) {
                        fieldsNames.add(var.getId().getName());
                    }
                    FieldDeclaration tmp = new FieldDeclaration(ModifierSet.PUBLIC, field.getType(), field.getVariables());
                    ASTHelper.addMember(declaration, tmp);
                }
            }
        }
        if (parameters != null) {
            for (Parameter param :
                    parameters) {
                FieldDeclaration fieldDeclaration =
                        new FieldDeclaration(ModifierSet.PUBLIC, param.getType(),
                                getFieldNameFromParam(param.getId(), fieldsNames));
                ASTHelper.addMember(declaration, fieldDeclaration);
            }
        }
        return inputCu;
    }

    /**
     * If parameter has the same name as one of the field from field list, changes the name of parameter adding '1' to the end of the name(recursively)
     * @param param is checked parameter
     * @param fieldsNames is the list of String which contains the fields names
     * @return updated variable declarator of the future field
     */
    public static VariableDeclarator getFieldNameFromParam(VariableDeclaratorId param, ArrayList<String> fieldsNames) {
        String result = param.getName();
        if (fieldsNames.contains(result)) {
            result = result + "1";
            param.setName(result);
            getFieldNameFromParam(param, fieldsNames);
        }
        return new VariableDeclarator(param);
    }

    /**
     * Creates output class for Lambda function based on the method
     * @param methodEntity is a basic method for Lambda function and creating the input class
     * @return Compilation unit object of the created class
     */
    private static CompilationUnit createOutPutType(MethodEntity methodEntity) {
        CompilationUnit outputCu = new CompilationUnit();
        ClassOrInterfaceDeclaration declaration =
                new ClassOrInterfaceDeclaration(ModifierSet.PUBLIC, false, "OutputType");
        ASTHelper.addTypeDeclaration(outputCu, declaration);
        List<FieldDeclaration> fields = methodEntity.getClassEntity().getFields();
        for (FieldDeclaration field :
                fields) {
            if (!ModifierSet.isFinal(field.getModifiers())) {
                FieldDeclaration tmp = new FieldDeclaration(ModifierSet.PUBLIC, field.getType(), field.getVariables());
                ASTHelper.addMember(declaration, tmp);
            }
        }
        Type returnType = methodEntity.getMethodDeclaration().getType();
        if (!returnType.equals(ASTHelper.VOID_TYPE)) {
            FieldDeclaration fieldDeclaration =
                    new FieldDeclaration(ModifierSet.PUBLIC, returnType, new VariableDeclarator(
                            new VariableDeclaratorId(methodEntity.getMethodDeclaration().getName() + "Result")));
            ASTHelper.addMember(declaration, fieldDeclaration);
        }
        if (methodEntity.getMethodDeclaration().getThrows() != null) {
            Type exception = new ClassOrInterfaceType("Exception");
            FieldDeclaration exceptionField = new FieldDeclaration(ModifierSet.PUBLIC, exception,
                    new VariableDeclarator(new VariableDeclaratorId("LambdaException")));
            ASTHelper.addMember(declaration, exceptionField);
            Type exceptionTypeString = new ClassOrInterfaceType("String");
            FieldDeclaration exceptionTypeField = new FieldDeclaration(ModifierSet.PUBLIC, exceptionTypeString,
                    new VariableDeclarator(new VariableDeclaratorId("LambdaExceptionType")));
            ASTHelper.addMember(declaration, exceptionTypeField);
        }
        return outputCu;

    }

    /**
     * Generates the list of imports taking the imports of method's class
     * @param methodEntity is method to generate imports for (during Lambda creating)
     * @return the ArrayList of imports
     */
    public static List<ImportDeclaration> generateImportsList(MethodEntity methodEntity) {
        CompilationUnit cu = methodEntity.getClassEntity().getCu();
        List<ImportDeclaration> imports;
        if (methodEntity.getClassEntity().getCu().getImports() != null) {
            imports = new ArrayList<>(methodEntity.getClassEntity().getCu().getImports());
        } else {
            imports = new ArrayList<>();
        }
        if (cu.getPackage() != null) {
            String packageName = cu.getPackage().getName().toString() + ".*";
            ImportDeclaration selfImport = new ImportDeclaration(new NameExpr(packageName), false, false);
            if (!imports.contains(selfImport)) {
                imports.add(selfImport);
            }
        }else {
            String packageName = Constants.EXTRA_PACKAGE;
            ImportDeclaration selfImport = new ImportDeclaration(new NameExpr(packageName), false, true);
            if (!imports.contains(selfImport)) {
                imports.add(selfImport);
            }
        }
        return imports;
    }

    /**
     * Generates the package name for Lambda function's input and output classes
     * @param methodEntity is a method to upload as Lambda function
     * @param isForCloud is a flag which defines the locations of package(or lambda project or method invoker location)
     * @return generated PackageDeclaration object
     */
    private static PackageDeclaration generatePackage(MethodEntity methodEntity, boolean isForCloud) {
        CompilationUnit compilationUnit = methodEntity.getClassEntity().getCu();
        MethodDeclaration methodDeclaration = methodEntity.getMethodDeclaration();
        String oldPackage = "";
        if (compilationUnit.getPackage() != null) {
            oldPackage = "." + compilationUnit.getPackage().getName().toString();
        }
        String methodPackage = methodDeclaration.getName();
        if (methodDeclaration.getParameters() != null) {
            methodPackage = methodPackage + methodDeclaration.getParameters().size();
        }
        NameExpr packageNameExpr;
        if (isForCloud) {
            packageNameExpr = new NameExpr(Constants.FUNCTION_PACKAGE);
        } else {
            packageNameExpr = new NameExpr("awsl" +
                    oldPackage + "." + compilationUnit.getTypes().get(0).getName() + "." +
                    methodPackage);
        }
        return new PackageDeclaration(packageNameExpr);
    }

    /**
     * Adds the access methods to the class.
     * @param compilationUnit object represents the class where to add methods.
     * @param methodEntity method for which input and output classes will be created
     * @param isForCloud is a flag which defines the locations of class(or lambda project or method invoker location)
     * @return CompilationUnit object with needed access methods
     */
    private static CompilationUnit createGetSet(CompilationUnit compilationUnit, MethodEntity methodEntity, boolean isForCloud) {
        if (isForCloud) {
            compilationUnit.setImports(generateImportsList(methodEntity));
        } else {
            compilationUnit.setImports(generateImportsList(methodEntity));
        }
        compilationUnit.setPackage(generatePackage(methodEntity, isForCloud));

        FieldsVisitor fieldsVisitor = new FieldsVisitor();
        fieldsVisitor.visit(compilationUnit, null);
        List<FieldDeclaration> fieldDeclarationList = fieldsVisitor.getFieldDeclarationList();
        ConstructorDeclaration constructor = new ConstructorDeclaration(ModifierSet.PUBLIC,
                compilationUnit.getTypes().get(0).getName());
        List<Parameter> constrParameters = new LinkedList<>();
        BlockStmt constrBlock = new BlockStmt();

        constructor.setBlock(constrBlock);
        if (fieldDeclarationList.size() > 0) {
            ConstructorDeclaration emptyConstructor = new ConstructorDeclaration(ModifierSet.PUBLIC,
                    compilationUnit.getTypes().get(0).getName());
            BlockStmt emptyConstrBlock = new BlockStmt();
            emptyConstructor.setBlock(emptyConstrBlock);
            ASTHelper.addMember(compilationUnit.getTypes().get(0), emptyConstructor);
        }
        for (FieldDeclaration field :
                fieldDeclarationList) {
            for (VariableDeclarator var :
                    field.getVariables()) {
                Parameter constrParam = new Parameter(field.getType(), var.getId());
                constrParameters.add(constrParam);
                FieldAccessExpr fieldAccessExpr = new FieldAccessExpr(new ThisExpr(), var.getId().getName());
                AssignExpr assignExprConstr =
                        new AssignExpr(fieldAccessExpr, new NameExpr(var.getId().getName()), AssignExpr.Operator.assign);
                ASTHelper.addStmt(constrBlock, assignExprConstr);
                MethodDeclaration setter =
                        new MethodDeclaration(ModifierSet.PUBLIC, ASTHelper.VOID_TYPE, "set" +
                                firstLetterToUpperCase(var.getId().getName()));
                BlockStmt setBlock = new BlockStmt();
                setter.setBody(setBlock);
                FieldAccessExpr fieldExpr = new FieldAccessExpr(new ThisExpr(), var.getId().getName());
                AssignExpr assignExpr = new AssignExpr(fieldExpr, new NameExpr(var.getId().getName()), AssignExpr.Operator.assign);
                ASTHelper.addStmt(setBlock, assignExpr);
                Parameter param = new Parameter(field.getType(), var.getId());
                ASTHelper.addParameter(setter, param);
                ASTHelper.addMember(compilationUnit.getTypes().get(0), setter);

                //if arrays type defined near the variable declaration add arrays declaration to return type
                Type returnType = field.getType();
                if (var.getId().toString().endsWith("[]")){
                    String varString = var.getId().toString();
                    String arraysScopes = varString.substring(var.getId().getName().length(), varString.length());
                    returnType = new ClassOrInterfaceType(returnType.toString() + arraysScopes);
                }

                MethodDeclaration getter =
                        new MethodDeclaration(ModifierSet.PUBLIC, returnType, "get" +
                                firstLetterToUpperCase(var.getId().getName()));
                BlockStmt getBlock = new BlockStmt();
                NameExpr returnExpr = new NameExpr("return " + var.getId().getName());
                ASTHelper.addStmt(getBlock, returnExpr);
                getter.setBody(getBlock);
                ASTHelper.addMember(compilationUnit.getTypes().get(0), getter);
            }
        }
        constructor.setParameters(constrParameters);
        ASTHelper.addMember(compilationUnit.getTypes().get(0), constructor);
        return compilationUnit;
    }
    private static class FieldsVisitor extends VoidVisitorAdapter {
        private List<FieldDeclaration> fieldDeclarationList = new ArrayList<>();

        @Override
        public void visit(FieldDeclaration n, Object arg) {
            fieldDeclarationList.add(n);
            super.visit(n, arg);
        }

        List<FieldDeclaration> getFieldDeclarationList() {
            return fieldDeclarationList;
        }
    }

    public static CompilationUnit getInputClass(MethodEntity methodEntity, boolean isForCloud) {
        return createGetSet(createInPutType(methodEntity), methodEntity, isForCloud);
    }

    public static CompilationUnit getOutputClass(MethodEntity methodEntity, boolean isForCloud) {
        return createGetSet(createOutPutType(methodEntity), methodEntity, isForCloud);
    }

    public static List<FieldDeclaration> getOnlyStaticFields(List<FieldDeclaration> fields) {
        List<FieldDeclaration> staticFields = new ArrayList<>();
        for (FieldDeclaration field :
                fields) {
            if (ModifierSet.isStatic(field.getModifiers())) {
                staticFields.add(field);
            }
        }
        return staticFields;
    }

    /**
     * Translates methods into invokers in the appropriate class
     * @param classEntity is  a class to translate
     * @param newPath config path
     * @return translated Compilation unit object
     */
    public static CompilationUnit translateClass(ClassEntity classEntity, String newPath) {
        CompilationUnit cu = classEntity.getCu();
        ClassOrInterfaceDeclaration declaration = (ClassOrInterfaceDeclaration) cu.getTypes().get(0);
        int i = 0;
        //if cu has some inner class i++
        for (Node node :
                cu.getTypes().get(0).getChildrenNodes()) {
            if (node instanceof ClassOrInterfaceDeclaration) {
                i++;
            }
        }
        if (!declaration.isInterface() & i == 0) {
            InvokeClassTranslator invokeClassTranslator = new InvokeClassTranslator(cu);
            invokeClassTranslator.addBufferByteReaderMethod();
            invokeClassTranslator.generateImports();
            List<MethodEntity> methodEntityList = classEntity.getFunctions();
            for (MethodEntity methodEntity :
                    methodEntityList) {
                if (methodEntity.getMethodDeclaration().getBody() == null || methodEntity.getMethodDeclaration().getBody().getStmts() == null){
                    continue;
                }
                //if the method has more then one line of code
                if (!UtilityClass.isAccessMethod(methodEntity.getMethodDeclaration()) &
                        !methodEntity.getMethodDeclaration().getName().equals("main")) {
                    InvokeMethodCreator invokeMethodCreator = new InvokeMethodCreator(methodEntity, newPath);
                    invokeMethodCreator.createMethodInvoker();
                }
            }
            return invokeClassTranslator.getCompilationUnit();
        }
        return cu;
    }

    public static CompilationUnit translateClassFunction(ClassEntity classEntity, MethodEntity unchangedMethod, String newPath) {
        CompilationUnit cu = classEntity.getCu();
        ClassOrInterfaceDeclaration declaration = (ClassOrInterfaceDeclaration) cu.getTypes().get(0);
        int i = 0;
        for (Node node :
                cu.getTypes().get(0).getChildrenNodes()) {
            if (node instanceof ClassOrInterfaceDeclaration) {
                i++;
            }
        }
        if (!declaration.isInterface() & i == 0) {
            InvokeClassTranslator invokeClassTranslator = new InvokeClassTranslator(cu);
            invokeClassTranslator.addBufferByteReaderMethod();
            invokeClassTranslator.generateImports();
            List<MethodEntity> methodEntityList = classEntity.getFunctions();
            for (MethodEntity methodEntity :
                    methodEntityList) {
                //if the method has more then one line of code
                if (methodEntity.getMethodDeclaration().getBody().getStmts().size() > 1 &
                        !methodEntity.getMethodDeclaration().equals(unchangedMethod.getMethodDeclaration())) {
                    InvokeMethodCreator invokeMethodCreator = new InvokeMethodCreator(methodEntity, newPath);
                    invokeMethodCreator.createMethodInvoker();
                }
            }
            return invokeClassTranslator.getCompilationUnit();
        }
        return cu;
    }

    /**
     * Changes all method's access modifiers to 'PUBLIC'
     * @param classEntity is {@code {@link ClassEntity}} instance of the class
     */
    public static void makeAllMethodsPublic(ClassEntity classEntity) {
        List<MethodEntity> methods = classEntity.getFunctions();
        for (MethodEntity method :
                methods) {
            makeMethodPublic(method.getMethodDeclaration());
        }
    }

    /**
     * Changes all constructor's access modifiers to 'PUBLIC'
     * @param classEntity is {@code {@link ClassEntity}} instance of the class
     */
    public static void makeConstructorsPublic(ClassEntity classEntity){
        CompilationUnit cu = classEntity.getCu();
        List<BodyDeclaration> bodyDeclarations = cu.getTypes().get(0).getMembers();
        for (BodyDeclaration member :
                bodyDeclarations) {
            if (member instanceof ConstructorDeclaration) {
                makeConstructorPublic((ConstructorDeclaration) member);
            }
        }
    }

    /**
     * Checks is the methods is access method(getter ar setter)
     * @param methodDeclaration is {@code {@link MethodDeclaration}} instance of the method to change
     * @return {@code true} if it's access method and {@code false} if it's not
     */
    public static boolean isAccessMethod(MethodDeclaration methodDeclaration){
        String str = methodDeclaration.getName().substring(0, 3);
        if (str.equals("set") | str.equals("get")){
            if (methodDeclaration.getBody().getStmts().size() < 2){
                return true;
            }
        }
        return false;
    }

    /**
     * Adds JSon annotations to fields and access methods in the class
     * @param classEntity is class to be edited
     */
    public static void addJsonAnnotations(ClassEntity classEntity){
        MarkerAnnotationExpr propAnnotationExpr = new MarkerAnnotationExpr(new NameExpr("JsonProperty"));
        MarkerAnnotationExpr ignoreAnnotationExpr = new MarkerAnnotationExpr(new NameExpr("JsonIgnore"));
        List<AnnotationExpr> propAnnotations = new ArrayList<>();
        propAnnotations.add(propAnnotationExpr);
        List<AnnotationExpr> ignoreAnnotation = new ArrayList<>();
        ignoreAnnotation.add(ignoreAnnotationExpr);

        //import JSon annotations
        List<ImportDeclaration> imports = new ArrayList<>();
        if (classEntity.getCu().getImports() != null){
            imports.addAll(classEntity.getCu().getImports());
        }
        ImportDeclaration imd11 = new ImportDeclaration();
        imd11.setName(new NameExpr("com.fasterxml.jackson.annotation.*"));
        if (!imports.contains(imd11)){
            imports.add(imd11);
        }
        classEntity.getCu().setImports(imports);
        List<FieldDeclaration> fieldDeclarations = classEntity.getFields();
        for (FieldDeclaration field :
                fieldDeclarations) {
            List<AnnotationExpr> annotations = new ArrayList<>();
            if (field.getAnnotations() != null){
                annotations.addAll(field.getAnnotations());
            }
            annotations.addAll(propAnnotations);
            field.setAnnotations(annotations);
        }

        List<MethodEntity> methodEntities = classEntity.getFunctions();
        for (MethodEntity method :
                methodEntities) {
            MethodDeclaration methodDeclaration = method.getMethodDeclaration();
            String methodName = methodDeclaration.getName();
            if (methodName.startsWith("set") || methodName.startsWith("get")){
                List<AnnotationExpr> annotations = new ArrayList<>();
                if (methodDeclaration.getAnnotations() != null) {
                    annotations.addAll(methodDeclaration.getAnnotations());
                }
                annotations.addAll(ignoreAnnotation);
                methodDeclaration.setAnnotations(annotations);
            }
        }
        CompilationUnit cu = classEntity.getCu();
        addDefaultConstructor(cu);
    }

    /**
     * Adds default empty constructor to class if it does not exist
     * @param cu is {@code {@link CompilationUnit}} instance of the class
     */
    private static void addDefaultConstructor(CompilationUnit cu){
        for (TypeDeclaration typeDeclaration :
                cu.getTypes()) {
            if (!hasDefaultConstructor(typeDeclaration) && !((ClassOrInterfaceDeclaration)typeDeclaration).isInterface()
                    && ModifierSet.isAbstract(typeDeclaration.getModifiers())) {
                ConstructorDeclaration constructorDeclaration =
                        new ConstructorDeclaration(ModifierSet.PUBLIC, typeDeclaration.getName());
                BlockStmt constructorBlock = new BlockStmt();
                constructorDeclaration.setBlock(constructorBlock);
                ASTHelper.addMember(typeDeclaration, constructorDeclaration);

            }
        }
    }

    /**
     *
     * Checks if class has the default constructor declared
     * @param typeDeclaration is type declaration to check
     * @return {@code true} if class has default constructor
     */
    private static boolean hasDefaultConstructor(TypeDeclaration typeDeclaration){
        List<BodyDeclaration> members = typeDeclaration.getMembers();
        if (members != null){
            for (BodyDeclaration body :
                    members) {
                if (body instanceof ConstructorDeclaration) {
                    ConstructorDeclaration constructorDeclaration = (ConstructorDeclaration) body;
                    if (constructorDeclaration.getParameters() == null){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Generates the name of Lambda functions, based on the path in the file tree
     * @param path is generated file path for Lambda project
     * @param newPath is path of translated project
     * @return {@code {@link String}} instance of the generated name
     */

    public static String generateLambdaName(String path, String newPath) {
        String cut = "" + newPath + "/LambdaProjects/";
        path = path.substring(cut.length(), path.length());
        path = path.replace("/", "_");
        return path;
    }

    /**
     * Generates the name of Lambda functions, based on the method declaration
     * @param methodEntity is {@code {@link MethodDeclaration}} instance
     * @return {@code {@link String}} instance of the generated name
     */
    public static String generateLambdaName(MethodEntity methodEntity) {
        String packageName = "";
        ClassEntity classEntity = methodEntity.getClassEntity();
        if (classEntity.getCu().getPackage() != null) {
            packageName = classEntity.getCu().getPackage().getName().toString();
            packageName = packageName.replace('.', '_');
        }
        MethodDeclaration methodDeclaration = methodEntity.getMethodDeclaration();
        String className = classEntity.getCu().getTypes().get(0).getName();
        String functionName = "" + methodEntity.getMethodDeclaration().getName();
        if (methodDeclaration.getParameters() != null) {
            functionName = functionName + methodDeclaration.getParameters().size();
        }
        return "" + packageName + "_" + className + "_" + functionName;
    }

    /**
     * Changes any method's access modifier to {@code public}
     * @param methodDeclaration {@code {@link MethodDeclaration}} to be changed
     */
    public static void makeMethodPublic(MethodDeclaration methodDeclaration) {
        int modifiers = methodDeclaration.getModifiers();
        if (!ModifierSet.isPublic(modifiers)) {
            if (ModifierSet.isPrivate(modifiers)) {
                modifiers = ModifierSet.removeModifier(modifiers, ModifierSet.PRIVATE);
                modifiers = ModifierSet.addModifier(modifiers, ModifierSet.PUBLIC);
                methodDeclaration.setModifiers(modifiers);
            } else {
                if (ModifierSet.isProtected(modifiers)) {
                    modifiers = ModifierSet.removeModifier(modifiers, ModifierSet.PROTECTED);
                    modifiers = ModifierSet.addModifier(modifiers, ModifierSet.PUBLIC);
                    methodDeclaration.setModifiers(modifiers);
                } else {
                    modifiers = ModifierSet.addModifier(modifiers, ModifierSet.PUBLIC);
                    methodDeclaration.setModifiers(modifiers);
                }
            }
        }
    }

    /**
     * Changes constructor's access modifier to {@code public}
     * @param constructorDeclaration is {@code {@link ConstructorDeclaration}} instance to be changed
     */
    public static void makeConstructorPublic(ConstructorDeclaration constructorDeclaration) {
        int modifiers = constructorDeclaration.getModifiers();
        if (!ModifierSet.isPublic(modifiers)) {
            if (ModifierSet.isPrivate(modifiers)) {
                modifiers = ModifierSet.removeModifier(modifiers, ModifierSet.PRIVATE);
                modifiers = ModifierSet.addModifier(modifiers, ModifierSet.PUBLIC);
                constructorDeclaration.setModifiers(modifiers);
            } else {
                if (ModifierSet.isProtected(modifiers)) {
                    modifiers = ModifierSet.removeModifier(modifiers, ModifierSet.PROTECTED);
                    modifiers = ModifierSet.addModifier(modifiers, ModifierSet.PUBLIC);
                    constructorDeclaration.setModifiers(modifiers);
                } else {
                    modifiers = ModifierSet.addModifier(modifiers, ModifierSet.PUBLIC);
                    constructorDeclaration.setModifiers(modifiers);
                }
            }
        }
    }

    /**
     * Changes class's access modifier to {@code public}
     * @param cu is {@code {@link CompilationUnit}} instance of class
     */
    public static void makeClassPublic(CompilationUnit cu) {
        TypeDeclaration typeDeclaration = cu.getTypes().get(0);
        int modifiers = typeDeclaration.getModifiers();
        if (!ModifierSet.isPublic(modifiers)) {
            if (ModifierSet.isPrivate(modifiers)) {
                modifiers = ModifierSet.removeModifier(modifiers, ModifierSet.PRIVATE);
                modifiers = ModifierSet.addModifier(modifiers, ModifierSet.PUBLIC);
                typeDeclaration.setModifiers(modifiers);
            } else {
                if (ModifierSet.isProtected(modifiers)) {
                    modifiers = ModifierSet.removeModifier(modifiers, ModifierSet.PROTECTED);
                    modifiers = ModifierSet.addModifier(modifiers, ModifierSet.PUBLIC);
                    typeDeclaration.setModifiers(modifiers);
                } else {
                    modifiers = ModifierSet.addModifier(modifiers, ModifierSet.PUBLIC);
                    typeDeclaration.setModifiers(modifiers);
                }
            }
        }
    }

    private static FieldDeclaration createPrivateStringField(String name){
        return new FieldDeclaration(ModifierSet.PRIVATE,
                new ClassOrInterfaceType("String"), new VariableDeclarator(new VariableDeclaratorId(name)));
    }
    public static CompilationUnit createConfigEntity(){
        ClassOrInterfaceDeclaration innerConfig = new ClassOrInterfaceDeclaration(ModifierSet.PUBLIC, false, "AWSConfEntity");
        String[] fieldNames = new String[]{"awsAccessKeyId", "awsSecretAccessKey", "awsRole", "awsRegion"};
        List<FieldDeclaration> fields = new ArrayList<>();
        for (int i = 0; i < fieldNames.length; i++){
            ASTHelper.addMember(innerConfig, createPrivateStringField(fieldNames[i]));
            fields.add(createPrivateStringField(fieldNames[i]));
        }
        for (FieldDeclaration field :
                fields) {
            ASTHelper.addMember(innerConfig, createGetterForSingleVar(field));
            ASTHelper.addMember(innerConfig, createSetterForSingleVar(field));
        }
        CompilationUnit compilationUnit1 = new CompilationUnit();
        ASTHelper.addTypeDeclaration(compilationUnit1, innerConfig);
        compilationUnit1.setPackage(new PackageDeclaration(new NameExpr("awsl")));
        return compilationUnit1;
    }
    private static MethodDeclaration createSetterForSingleVar(FieldDeclaration field){
        VariableDeclaratorId varId = field.getVariables().get(0).getId();
        Parameter parameter = new Parameter(field.getType(), varId);
        List<Parameter> parameters = new ArrayList<>();
        parameters.add(parameter);
        MethodDeclaration result = new MethodDeclaration(ModifierSet.PUBLIC, ASTHelper.VOID_TYPE,
                "set" + UtilityClass.firstLetterToUpperCase(varId.getName()), parameters);
        NameExpr methodBodyExpr = new NameExpr("this." + varId.getName() + " = " + varId.getName());
        BlockStmt blockStmt = new BlockStmt();
        ASTHelper.addStmt(blockStmt, methodBodyExpr);
        result.setBody(blockStmt);
        return result;
    }
    private static MethodDeclaration createGetterForSingleVar(FieldDeclaration field){
        VariableDeclaratorId varId = field.getVariables().get(0).getId();
        MethodDeclaration result = new MethodDeclaration(ModifierSet.PUBLIC, field.getType(),
                "get" + UtilityClass.firstLetterToUpperCase(varId.getName()));
        NameExpr methodBodyExpr = new NameExpr("return " + varId.getName());
        BlockStmt blockStmt = new BlockStmt();
        ASTHelper.addStmt(blockStmt, methodBodyExpr);
        result.setBody(blockStmt);
        return result;
    }
}
