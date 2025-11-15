{
  inputs = {
    typelevel-nix.url = "github:typelevel/typelevel-nix";
    nixpkgs.follows = "typelevel-nix/nixpkgs";
    flake-utils.follows = "typelevel-nix/flake-utils";
  };

  outputs = { self, nixpkgs, flake-utils, typelevel-nix }:
    flake-utils.lib.eachDefaultSystem (system:
      let
        pkgs = import nixpkgs {
          inherit system;
          overlays = [ typelevel-nix.overlays.default ];
        };

        inherit (pkgs) lib stdenv;

        nativeImageDrv = stdenv.mkDerivation {
          pname = "devops-cli-native";
          version = "0.1.0";
          src = self;

          nativeBuildInputs = with pkgs; [ mill graalvm-ce ];
          buildInputs = lib.optionals stdenv.isDarwin (with pkgs; [ zlib libiconv ]);

          buildPhase = ''
            export MILL_CONSENT=YES
            export JAVA_HOME=${pkgs.graalvm-ce}
            export GRAALVM_HOME=${pkgs.graalvm-ce}
            export PATH=$GRAALVM_HOME/bin:$PATH
            export HOME=$TMPDIR
            export MILL_HOME=$TMPDIR/.mill
            mill -i cli.nativeImage.build
          '';

          installPhase = ''
            install -Dm755 out/cli/nativeImage/build.dest/devops-cli $out/bin/devops-cli
          '';
        };
      in {
        packages = {
          cli-native-image = nativeImageDrv;
          default = nativeImageDrv;
        };

        apps = {
          cli-native = flake-utils.lib.mkApp { drv = nativeImageDrv; };
          default = flake-utils.lib.mkApp { drv = nativeImageDrv; };
        };

        devShell = pkgs.devshell.mkShell {
          imports = [ typelevel-nix.typelevelShell ];
          name = "kubernetes-client";

          typelevelShell = {
            # Use GraalVM for native-image support
            jdk.package = pkgs.jdk17;

            # Enable native compilation support with required libraries
            native = {
              enable = true;
              libraries = with pkgs; [ zlib ];  # Add zlib for native image linking
            };

            # Enable Node.js for any JS-based tooling
            nodejs.enable = true;
          };

          # Additional packages for development
          packages = with pkgs; [
            coursier      # Scala dependency management
          ];
        };
      });
}

