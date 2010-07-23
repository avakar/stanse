#include "ast_dumper.hpp"

#include <clang/Sema/ParseAST.h>
#include <clang/Lex/Preprocessor.h>
#include <clang/Basic/SourceManager.h>
#include <clang/Basic/FileManager.h>
#include <clang/Basic/TargetInfo.h>
#include <clang/Lex/HeaderSearch.h>
#include <clang/Ast/ASTContext.h>
#include <clang/Frontend/CompilerInvocation.h>
#include <clang/Ast/ASTConsumer.h>
#include <clang/Ast/Decl.h>
#include <clang/Ast/Stmt.h>
#include <clang/Ast/DeclTemplate.h>
#include <clang/Ast/DeclCXX.h>
#include <clang/Analysis/CFG.h>
#include <clang/Frontend/Utils.h>
#include <clang/Frontend/CompilerInstance.h>
#include <clang/Frontend/FrontendActions.h>
#include <clang/Frontend/TextDiagnosticBuffer.h>

#include <iostream>
#include <set>

#include <boost/utility.hpp>
#include <boost/assert.hpp>

class MyConsumer
	: public clang::ASTConsumer
{
public:
	MyConsumer(clang::CompilerInstance & ci, bool printAST, bool printCFG, bool printReadableAST)
		: m_ci(ci), printAST(printAST), printCFG(printCFG), printReadableAST(printReadableAST)
	{
	}

	void HandleTranslationUnit(clang::ASTContext &ctx)
	{
		if (m_ci.getDiagnostics().getNumErrors() > 0)
		{
			std::cerr << "Errors were found, serialization of AST and CFGs is disabled." << std::endl;
			return;
		}

		std::set<clang::FunctionDecl const *> functionDecls;
		get_used_function_defs(ctx, functionDecls);

		if (printAST)
			print_ast(std::cout, ctx, functionDecls.begin(), functionDecls.end());

		if (printCFG)
			print_cfg(std::cout, ctx, functionDecls.begin(), functionDecls.end());

		if (printReadableAST)
			print_readable_ast(std::cout, ctx, functionDecls.begin(), functionDecls.end());
	}

private:
	clang::CompilerInstance & m_ci;
	bool printAST;
	bool printCFG;
	bool printReadableAST;
};

class MyASTDumpAction : public clang::ASTFrontendAction
{
public:
	MyASTDumpAction(bool printAST, bool printCFG, bool printReadableAST)
		: printAST(printAST), printCFG(printCFG), printReadableAST(printReadableAST)
	{
	}

protected:
	virtual clang::ASTConsumer *CreateASTConsumer(clang::CompilerInstance &CI,
		llvm::StringRef InFile)
	{
		return new MyConsumer(CI, printAST, printCFG, printReadableAST);
	}

private:
	bool printAST;
	bool printCFG;
	bool printReadableAST;
};

int main(int argc, char * argv[])
{
/*	MyDiagClient diag_client;
	clang::Diagnostic diag(&diag_client);*/

	char const * additional_args[] = {
		"-triple", "i686-pc-win32",
		//"-disable-free",
		//"-main-file-name", "pokus.cpp",
		//"-mrelocation-model", "static",
		//"-mdisable-fp-elim",
		//"-mconstructor-aliases",
		//"-resource-dir", "x:/checkouts/llvm/bin/lib/clang/2.8",
		"-ferror-limit", "19",
		"-fmessage-length", "300",
		"-fexceptions",
		"-fms-extensions",
		"-fgnu-runtime",
		"-fdiagnostics-show-option",
		"-fcolor-diagnostics",
		//"-fsyntax-only",
		"-x", "c++",
		//"c:\\users\\Martin.vejnar\\Documents\\temp\\pokus.cpp"
	};

	std::vector<char const *> args(additional_args, additional_args + sizeof additional_args / sizeof additional_args[0]);

	bool printAST = false;
	bool printCFG = false;
	bool printReadableAST = false;
	std::vector<clang::DirectoryLookup> includePaths;

	// Parse the arguments
	for (int i = 1; i < argc; ++i)
	{
		std::string arg = argv[i];
		if (arg == "-a")
			printReadableAST = true;
		else if (arg == "-A")
			printAST = true;
		else if (arg == "-c")
			printCFG = true;
		else
			args.push_back(argv[i]);
	}

	clang::CompilerInstance comp_inst;

	clang::TextDiagnosticBuffer argDiagBuffer;
	clang::Diagnostic argDiag(&argDiagBuffer);
	clang::CompilerInvocation & ci = comp_inst.getInvocation();
	clang::CompilerInvocation::CreateFromArgs(ci, &args.front(), &args.back() + 1, argDiag);
	comp_inst.createDiagnostics(args.size(), (char **)&args[0]);
	argDiagBuffer.FlushDiagnostics(comp_inst.getDiagnostics());

	comp_inst.createFileManager();

	if (!printReadableAST && !printAST && !printCFG)
		printReadableAST = true;

	MyASTDumpAction act(printAST, printCFG, printReadableAST);
	comp_inst.ExecuteAction(act);

/*
	comp_inst.createSourceManager();
	comp_inst.createFileManager();

	clang::SourceManager & sm = comp_inst.getSourceManager();
	clang::FileManager & fm = comp_inst.getFileManager();
*/

/*
	sm.createMainFileID(
		fm.getFile(targetFile),
		clang::SourceLocation());

	clang::HeaderSearch hs(fm);
	hs.SetSearchPaths(includePaths, 0, false);*

	clang::TargetInfo * pTi = clang::TargetInfo::CreateTargetInfo(comp_inst.getDiagnostics(), ci.getTargetOpts());
	comp_inst.setTarget(pTi);

	comp_inst.createPreprocessor();

	clang::Preprocessor & pp = comp_inst.getPreprocessor();

	pp.getBuiltinInfo().InitializeBuiltins(pp.getIdentifierTable(), pp.getLangOptions().NoBuiltin);
	InitializePreprocessor(pp, ci.getPreprocessorOpts(), ci.getHeaderSearchOpts(), ci.getFrontendOpts());

	clang::ASTContext ctx(ci.getLangOpts(), pp.getSourceManager(), pp.getTargetInfo(), pp.getIdentifierTable(), pp.getSelectorTable(), pp.getBuiltinInfo());
	
	MyConsumer consumer;

	clang::ParseAST(pp, &consumer, ctx);
*/

	//clang::ASTContext & ctx = comp_inst.getASTContext();

	// Scan the AST and retrieve the set of top-level functions and function template instantiations that are (transitively)
	// used by these top-level functions.
	// TODO: member function should be found too.

}