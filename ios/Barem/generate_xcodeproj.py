#!/usr/bin/env python3
"""Generate Barem.xcodeproj for opening in Xcode on macOS."""
from pathlib import Path
import uuid

ROOT = Path(__file__).parent
BAREM = ROOT / "Barem"
ENGINE = BAREM / "Resources" / "engine"

SWIFT_FILES = [
    "Barem/BaremApp.swift",
    "Barem/RootTabView.swift",
    "Barem/WelcomeView.swift",
    "Barem/GorusViews.swift",
    "Barem/StatisticsViews.swift",
    "Barem/MemurViews.swift",
    "Barem/BaremComponents.swift",
    "Barem/PremiumManager.swift",
    "Barem/AnalyticsChartsView.swift",
    "Barem/CalcSession.swift",
    "Barem/Engine/EngineModels.swift",
    "Barem/Engine/EngineRepository.swift",
    "Barem/Engine/StatKind.swift",
    "Barem/Engine/PeriodColumnResolver.swift",
    "Barem/Engine/MemurEngine.swift",
    "Barem/Engine/MemurCatalog.swift",
    "Barem/Engine/YearlyCalcEngine.swift",
    "Barem/Engine/VdLookupRepository.swift",
    "Barem/Engine/CalcHistoryRepository.swift",
]

def uid():
    return uuid.uuid4().hex[:24].upper()

project_id = uid()
target_id = uid()
sources_phase = uid()
resources_phase = uid()
frameworks_phase = uid()
product_ref = uid()
build_config_list_proj = uid()
build_config_list_tgt = uid()
debug_cfg_proj = uid()
release_cfg_proj = uid()
debug_cfg_tgt = uid()
release_cfg_tgt = uid()

file_refs = {}
build_files_swift = []
build_files_res = []

for rel in SWIFT_FILES:
    fid = uid()
    bid = uid()
    file_refs[rel] = fid
    build_files_swift.append((bid, fid))

engine_folder_ref = uid()
engine_build_ref = uid()

pbx = f'''// !$*UTF8*$!
{{
\tarchiveVersion = 1;
\tclasses = {{}};
\tobjectVersion = 56;
\tobjects = {{

/* Begin PBXBuildFile section */
'''
for bid, fid in build_files_swift:
    name = [r for r, i in file_refs.items() if i == fid][0].split("/")[-1]
    pbx += f"\t\t{bid} /* {name} in Sources */ = {{isa = PBXBuildFile; fileRef = {fid} /* {name} */; }};\n"

pbx += f"\t\t{engine_build_ref} /* engine in Resources */ = {{isa = PBXBuildFile; fileRef = {engine_folder_ref} /* engine */; }};\n"
pbx += "/* End PBXBuildFile section */\n\n"

pbx += "/* Begin PBXFileReference section */\n"
pbx += f"\t\t{product_ref} /* Barem.app */ = {{isa = PBXFileReference; explicitFileType = wrapper.application; includeInIndex = 0; path = Barem.app; sourceTree = BUILT_PRODUCTS_DIR; }};\n"
for rel, fid in file_refs.items():
    name = rel.split("/")[-1]
    pbx += f"\t\t{fid} /* {name} */ = {{isa = PBXFileReference; lastKnownFileType = sourcecode.swift; path = {name}; sourceTree = \"<group>\"; }};\n"
pbx += f"\t\t{engine_folder_ref} /* engine */ = {{isa = PBXFileReference; lastKnownFileType = folder; path = engine; sourceTree = \"<group>\"; }};\n"
info_plist = uid()
pbx += f"\t\t{info_plist} /* Info.plist */ = {{isa = PBXFileReference; lastKnownFileType = text.plist.xml; path = Info.plist; sourceTree = \"<group>\"; }};\n"
pbx += "/* End PBXFileReference section */\n\n"

pbx += f'''/* Begin PBXFrameworksBuildPhase section */
\t\t{frameworks_phase} /* Frameworks */ = {{
\t\t\tisa = PBXFrameworksBuildPhase;
\t\t\tbuildActionMask = 2147483647;
\t\t\tfiles = (
\t\t\t);
\t\t\trunOnlyForDeploymentPostprocessing = 0;
\t\t}};
/* End PBXFrameworksBuildPhase section */

/* Begin PBXGroup section */
\t\t{uid()} = {{
\t\t\tisa = PBXGroup;
\t\t\tchildren = (
\t\t\t\t{uid()} /* Barem */,
\t\t\t\t{uid()} /* Products */,
\t\t\t);
\t\t\tsourceTree = "<group>";
\t\t}};
'''

# Simplified - write full project manually with fixed structure
print("Use Barem.xcodeproj/project.pbxproj template")
