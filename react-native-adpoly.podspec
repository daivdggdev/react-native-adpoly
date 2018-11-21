require 'json'

package = JSON.parse(File.read(File.join(__dir__, 'package.json')))

Pod::Spec.new do |s|
  s.name         = "react-native-adpoly"
  s.version      = package['version'].gsub(/v|-beta/, '')
  s.summary      = package['description']
  s.author       = package['author']
  s.license      = package['license']
  s.homepage     = package['homepage']
  s.platform     = :ios, "8.0"
  s.source       = { :git => "https://github.com/daivdggdev/react-native-adpoly", :tag => "v#{s.version}" }
  s.source_files = "ios/RNAdPoly/*.{h,m}"

  s.subspec 'GDT' do |gdt|
    gdt.source_files = "ios/Libraries/GDT_iOS_SDK_4.8.1/*.h"
    gdt.vendored_libraries = "ios/Libraries/GDT_iOS_SDK_4.8.1/libGDTMobSDK.a"
    gdt.libraries = "GDTMobSDK"
  end

  s.dependency 'React'
  s.dependency 'Masonry'
end
