package codec

import chisel3._

object EncodeTestTable {
    val D_x_y = Seq(
        Seq("h00".U, "b0010_111001".U, "b1101_000110".U, 0.U(1.W)),
        Seq("h01".U, "b0010_101110".U, "b1101_010001".U, 0.U(1.W)),
        Seq("h02".U, "b0010_101101".U, "b1101_010010".U, 0.U(1.W)),
        Seq("h03".U, "b1101_100011".U, "b0010_100011".U, 1.U(1.W)),
        Seq("h04".U, "b0010_101011".U, "b1101_010100".U, 0.U(1.W)),
        Seq("h05".U, "b1101_100101".U, "b0010_100101".U, 1.U(1.W)),
        Seq("h06".U, "b1101_100110".U, "b0010_100110".U, 1.U(1.W)),
        Seq("h07".U, "b1101_000111".U, "b0010_111000".U, 1.U(1.W)),
        Seq("h08".U, "b0010_100111".U, "b1101_011000".U, 0.U(1.W)),
        Seq("h09".U, "b1101_101001".U, "b0010_101001".U, 1.U(1.W)),
        Seq("h0A".U, "b1101_101010".U, "b0010_101010".U, 1.U(1.W)),
        Seq("h0B".U, "b1101_001011".U, "b0010_001011".U, 1.U(1.W)),
        Seq("h0C".U, "b1101_101100".U, "b0010_101100".U, 1.U(1.W)),
        Seq("h0D".U, "b1101_001101".U, "b0010_001101".U, 1.U(1.W)),
        Seq("h0E".U, "b1101_001110".U, "b0010_001110".U, 1.U(1.W)),
        Seq("h0F".U, "b0010_111010".U, "b1101_000101".U, 0.U(1.W)),
        Seq("h10".U, "b0010_110110".U, "b1101_001001".U, 0.U(1.W)),
        Seq("h11".U, "b1101_110001".U, "b0010_110001".U, 1.U(1.W)),
        Seq("h12".U, "b1101_110010".U, "b0010_110010".U, 1.U(1.W)),
        Seq("h13".U, "b1101_010011".U, "b0010_010011".U, 1.U(1.W)),
        Seq("h14".U, "b1101_110100".U, "b0010_110100".U, 1.U(1.W)),
        Seq("h15".U, "b1101_010101".U, "b0010_010101".U, 1.U(1.W)),
        Seq("h16".U, "b1101_010110".U, "b0010_010110".U, 1.U(1.W)),
        Seq("h17".U, "b0010_010111".U, "b1101_101000".U, 0.U(1.W)),
        Seq("h18".U, "b0010_110011".U, "b1101_001100".U, 0.U(1.W)),
        Seq("h19".U, "b1101_011001".U, "b0010_011001".U, 1.U(1.W)),
        Seq("h1A".U, "b1101_011010".U, "b0010_011010".U, 1.U(1.W)),
        Seq("h1B".U, "b0010_011011".U, "b1101_100100".U, 0.U(1.W)),
        Seq("h1C".U, "b1101_011100".U, "b0010_011100".U, 1.U(1.W)),
        Seq("h1D".U, "b0010_011101".U, "b1101_100010".U, 0.U(1.W)),
        Seq("h1E".U, "b0010_011110".U, "b1101_100001".U, 0.U(1.W)),
        Seq("h1F".U, "b0010_110101".U, "b1101_001010".U, 0.U(1.W)),
        Seq("h20".U, "b1001_111001".U, "b1001_000110".U, 1.U(1.W)),
        Seq("h21".U, "b1001_101110".U, "b1001_010001".U, 1.U(1.W)),
        Seq("h22".U, "b1001_101101".U, "b1001_010010".U, 1.U(1.W)),
        Seq("h23".U, "b1001_100011".U, "b1001_100011".U, 0.U(1.W)),
        Seq("h24".U, "b1001_101011".U, "b1001_010100".U, 1.U(1.W)),
        Seq("h25".U, "b1001_100101".U, "b1001_100101".U, 0.U(1.W)),
        Seq("h26".U, "b1001_100110".U, "b1001_100110".U, 0.U(1.W)),
        Seq("h27".U, "b1001_000111".U, "b1001_111000".U, 0.U(1.W)),
        Seq("h28".U, "b1001_100111".U, "b1001_011000".U, 1.U(1.W)),
        Seq("h29".U, "b1001_101001".U, "b1001_101001".U, 0.U(1.W)),
        Seq("h2A".U, "b1001_101010".U, "b1001_101010".U, 0.U(1.W)),
        Seq("h2B".U, "b1001_001011".U, "b1001_001011".U, 0.U(1.W)),
        Seq("h2C".U, "b1001_101100".U, "b1001_101100".U, 0.U(1.W)),
        Seq("h2D".U, "b1001_001101".U, "b1001_001101".U, 0.U(1.W)),
        Seq("h2E".U, "b1001_001110".U, "b1001_001110".U, 0.U(1.W)),
        Seq("h2F".U, "b1001_111010".U, "b1001_000101".U, 1.U(1.W)),
        Seq("h30".U, "b1001_110110".U, "b1001_001001".U, 1.U(1.W)),
        Seq("h31".U, "b1001_110001".U, "b1001_110001".U, 0.U(1.W)),
        Seq("h32".U, "b1001_110010".U, "b1001_110010".U, 0.U(1.W)),
        Seq("h33".U, "b1001_010011".U, "b1001_010011".U, 0.U(1.W)),
        Seq("h34".U, "b1001_110100".U, "b1001_110100".U, 0.U(1.W)),
        Seq("h35".U, "b1001_010101".U, "b1001_010101".U, 0.U(1.W)),
        Seq("h36".U, "b1001_010110".U, "b1001_010110".U, 0.U(1.W)),
        Seq("h37".U, "b1001_010111".U, "b1001_101000".U, 1.U(1.W)),
        Seq("h38".U, "b1001_110011".U, "b1001_001100".U, 1.U(1.W)),
        Seq("h39".U, "b1001_011001".U, "b1001_011001".U, 0.U(1.W)),
        Seq("h3A".U, "b1001_011010".U, "b1001_011010".U, 0.U(1.W)),
        Seq("h3B".U, "b1001_011011".U, "b1001_100100".U, 1.U(1.W)),
        Seq("h3C".U, "b1001_011100".U, "b1001_011100".U, 0.U(1.W)),
        Seq("h3D".U, "b1001_011101".U, "b1001_100010".U, 1.U(1.W)),
        Seq("h3E".U, "b1001_011110".U, "b1001_100001".U, 1.U(1.W)),
        Seq("h3F".U, "b1001_110101".U, "b1001_001010".U, 1.U(1.W)),
        Seq("h40".U, "b1010_111001".U, "b1010_000110".U, 1.U(1.W)),
        Seq("h41".U, "b1010_101110".U, "b1010_010001".U, 1.U(1.W)),
        Seq("h42".U, "b1010_101101".U, "b1010_010010".U, 1.U(1.W)),
        Seq("h43".U, "b1010_100011".U, "b1010_100011".U, 0.U(1.W)),
        Seq("h44".U, "b1010_101011".U, "b1010_010100".U, 1.U(1.W)),
        Seq("h45".U, "b1010_100101".U, "b1010_100101".U, 0.U(1.W)),
        Seq("h46".U, "b1010_100110".U, "b1010_100110".U, 0.U(1.W)),
        Seq("h47".U, "b1010_000111".U, "b1010_111000".U, 0.U(1.W)),
        Seq("h48".U, "b1010_100111".U, "b1010_011000".U, 1.U(1.W)),
        Seq("h49".U, "b1010_101001".U, "b1010_101001".U, 0.U(1.W)),
        Seq("h4A".U, "b1010_101010".U, "b1010_101010".U, 0.U(1.W)),
        Seq("h4B".U, "b1010_001011".U, "b1010_001011".U, 0.U(1.W)),
        Seq("h4C".U, "b1010_101100".U, "b1010_101100".U, 0.U(1.W)),
        Seq("h4D".U, "b1010_001101".U, "b1010_001101".U, 0.U(1.W)),
        Seq("h4E".U, "b1010_001110".U, "b1010_001110".U, 0.U(1.W)),
        Seq("h4F".U, "b1010_111010".U, "b1010_000101".U, 1.U(1.W)),
        Seq("h50".U, "b1010_110110".U, "b1010_001001".U, 1.U(1.W)),
        Seq("h51".U, "b1010_110001".U, "b1010_110001".U, 0.U(1.W)),
        Seq("h52".U, "b1010_110010".U, "b1010_110010".U, 0.U(1.W)),
        Seq("h53".U, "b1010_010011".U, "b1010_010011".U, 0.U(1.W)),
        Seq("h54".U, "b1010_110100".U, "b1010_110100".U, 0.U(1.W)),
        Seq("h55".U, "b1010_010101".U, "b1010_010101".U, 0.U(1.W)),
        Seq("h56".U, "b1010_010110".U, "b1010_010110".U, 0.U(1.W)),
        Seq("h57".U, "b1010_010111".U, "b1010_101000".U, 1.U(1.W)),
        Seq("h58".U, "b1010_110011".U, "b1010_001100".U, 1.U(1.W)),
        Seq("h59".U, "b1010_011001".U, "b1010_011001".U, 0.U(1.W)),
        Seq("h5A".U, "b1010_011010".U, "b1010_011010".U, 0.U(1.W)),
        Seq("h5B".U, "b1010_011011".U, "b1010_100100".U, 1.U(1.W)),
        Seq("h5C".U, "b1010_011100".U, "b1010_011100".U, 0.U(1.W)),
        Seq("h5D".U, "b1010_011101".U, "b1010_100010".U, 1.U(1.W)),
        Seq("h5E".U, "b1010_011110".U, "b1010_100001".U, 1.U(1.W)),
        Seq("h5F".U, "b1010_110101".U, "b1010_001010".U, 1.U(1.W)),
        Seq("h60".U, "b1100_111001".U, "b0011_000110".U, 1.U(1.W)),
        Seq("h61".U, "b1100_101110".U, "b0011_010001".U, 1.U(1.W)),
        Seq("h62".U, "b1100_101101".U, "b0011_010010".U, 1.U(1.W)),
        Seq("h63".U, "b0011_100011".U, "b1100_100011".U, 0.U(1.W)),
        Seq("h64".U, "b1100_101011".U, "b0011_010100".U, 1.U(1.W)),
        Seq("h65".U, "b0011_100101".U, "b1100_100101".U, 0.U(1.W)),
        Seq("h66".U, "b0011_100110".U, "b1100_100110".U, 0.U(1.W)),
        Seq("h67".U, "b0011_000111".U, "b1100_111000".U, 0.U(1.W)),
        Seq("h68".U, "b1100_100111".U, "b0011_011000".U, 1.U(1.W)),
        Seq("h69".U, "b0011_101001".U, "b1100_101001".U, 0.U(1.W)),
        Seq("h6A".U, "b0011_101010".U, "b1100_101010".U, 0.U(1.W)),
        Seq("h6B".U, "b0011_001011".U, "b1100_001011".U, 0.U(1.W)),
        Seq("h6C".U, "b0011_101100".U, "b1100_101100".U, 0.U(1.W)),
        Seq("h6D".U, "b0011_001101".U, "b1100_001101".U, 0.U(1.W)),
        Seq("h6E".U, "b0011_001110".U, "b1100_001110".U, 0.U(1.W)),
        Seq("h6F".U, "b1100_111010".U, "b0011_000101".U, 1.U(1.W)),
        Seq("h70".U, "b1100_110110".U, "b0011_001001".U, 1.U(1.W)),
        Seq("h71".U, "b0011_110001".U, "b1100_110001".U, 0.U(1.W)),
        Seq("h72".U, "b0011_110010".U, "b1100_110010".U, 0.U(1.W)),
        Seq("h73".U, "b0011_010011".U, "b1100_010011".U, 0.U(1.W)),
        Seq("h74".U, "b0011_110100".U, "b1100_110100".U, 0.U(1.W)),
        Seq("h75".U, "b0011_010101".U, "b1100_010101".U, 0.U(1.W)),
        Seq("h76".U, "b0011_010110".U, "b1100_010110".U, 0.U(1.W)),
        Seq("h77".U, "b1100_010111".U, "b0011_101000".U, 1.U(1.W)),
        Seq("h78".U, "b1100_110011".U, "b0011_001100".U, 1.U(1.W)),
        Seq("h79".U, "b0011_011001".U, "b1100_011001".U, 0.U(1.W)),
        Seq("h7A".U, "b0011_011010".U, "b1100_011010".U, 0.U(1.W)),
        Seq("h7B".U, "b1100_011011".U, "b0011_100100".U, 1.U(1.W)),
        Seq("h7C".U, "b0011_011100".U, "b1100_011100".U, 0.U(1.W)),
        Seq("h7D".U, "b1100_011101".U, "b0011_100010".U, 1.U(1.W)),
        Seq("h7E".U, "b1100_011110".U, "b0011_100001".U, 1.U(1.W)),
        Seq("h7F".U, "b1100_110101".U, "b0011_001010".U, 1.U(1.W)),
        Seq("h80".U, "b0100_111001".U, "b1011_000110".U, 0.U(1.W)),
        Seq("h81".U, "b0100_101110".U, "b1011_010001".U, 0.U(1.W)),
        Seq("h82".U, "b0100_101101".U, "b1011_010010".U, 0.U(1.W)),
        Seq("h83".U, "b1011_100011".U, "b0100_100011".U, 1.U(1.W)),
        Seq("h84".U, "b0100_101011".U, "b1011_010100".U, 0.U(1.W)),
        Seq("h85".U, "b1011_100101".U, "b0100_100101".U, 1.U(1.W)),
        Seq("h86".U, "b1011_100110".U, "b0100_100110".U, 1.U(1.W)),
        Seq("h87".U, "b1011_000111".U, "b0100_111000".U, 1.U(1.W)),
        Seq("h88".U, "b0100_100111".U, "b1011_011000".U, 0.U(1.W)),
        Seq("h89".U, "b1011_101001".U, "b0100_101001".U, 1.U(1.W)),
        Seq("h8A".U, "b1011_101010".U, "b0100_101010".U, 1.U(1.W)),
        Seq("h8B".U, "b1011_001011".U, "b0100_001011".U, 1.U(1.W)),
        Seq("h8C".U, "b1011_101100".U, "b0100_101100".U, 1.U(1.W)),
        Seq("h8D".U, "b1011_001101".U, "b0100_001101".U, 1.U(1.W)),
        Seq("h8E".U, "b1011_001110".U, "b0100_001110".U, 1.U(1.W)),
        Seq("h8F".U, "b0100_111010".U, "b1011_000101".U, 0.U(1.W)),
        Seq("h90".U, "b0100_110110".U, "b1011_001001".U, 0.U(1.W)),
        Seq("h91".U, "b1011_110001".U, "b0100_110001".U, 1.U(1.W)),
        Seq("h92".U, "b1011_110010".U, "b0100_110010".U, 1.U(1.W)),
        Seq("h93".U, "b1011_010011".U, "b0100_010011".U, 1.U(1.W)),
        Seq("h94".U, "b1011_110100".U, "b0100_110100".U, 1.U(1.W)),
        Seq("h95".U, "b1011_010101".U, "b0100_010101".U, 1.U(1.W)),
        Seq("h96".U, "b1011_010110".U, "b0100_010110".U, 1.U(1.W)),
        Seq("h97".U, "b0100_010111".U, "b1011_101000".U, 0.U(1.W)),
        Seq("h98".U, "b0100_110011".U, "b1011_001100".U, 0.U(1.W)),
        Seq("h99".U, "b1011_011001".U, "b0100_011001".U, 1.U(1.W)),
        Seq("h9A".U, "b1011_011010".U, "b0100_011010".U, 1.U(1.W)),
        Seq("h9B".U, "b0100_011011".U, "b1011_100100".U, 0.U(1.W)),
        Seq("h9C".U, "b1011_011100".U, "b0100_011100".U, 1.U(1.W)),
        Seq("h9D".U, "b0100_011101".U, "b1011_100010".U, 0.U(1.W)),
        Seq("h9E".U, "b0100_011110".U, "b1011_100001".U, 0.U(1.W)),
        Seq("h9F".U, "b0100_110101".U, "b1011_001010".U, 0.U(1.W)),
        Seq("hA0".U, "b0101_111001".U, "b0101_000110".U, 1.U(1.W)),
        Seq("hA1".U, "b0101_101110".U, "b0101_010001".U, 1.U(1.W)),
        Seq("hA2".U, "b0101_101101".U, "b0101_010010".U, 1.U(1.W)),
        Seq("hA3".U, "b0101_100011".U, "b0101_100011".U, 0.U(1.W)),
        Seq("hA4".U, "b0101_101011".U, "b0101_010100".U, 1.U(1.W)),
        Seq("hA5".U, "b0101_100101".U, "b0101_100101".U, 0.U(1.W)),
        Seq("hA6".U, "b0101_100110".U, "b0101_100110".U, 0.U(1.W)),
        Seq("hA7".U, "b0101_000111".U, "b0101_111000".U, 0.U(1.W)),
        Seq("hA8".U, "b0101_100111".U, "b0101_011000".U, 1.U(1.W)),
        Seq("hA9".U, "b0101_101001".U, "b0101_101001".U, 0.U(1.W)),
        Seq("hAA".U, "b0101_101010".U, "b0101_101010".U, 0.U(1.W)),
        Seq("hAB".U, "b0101_001011".U, "b0101_001011".U, 0.U(1.W)),
        Seq("hAC".U, "b0101_101100".U, "b0101_101100".U, 0.U(1.W)),
        Seq("hAD".U, "b0101_001101".U, "b0101_001101".U, 0.U(1.W)),
        Seq("hAE".U, "b0101_001110".U, "b0101_001110".U, 0.U(1.W)),
        Seq("hAF".U, "b0101_111010".U, "b0101_000101".U, 1.U(1.W)),
        Seq("hB0".U, "b0101_110110".U, "b0101_001001".U, 1.U(1.W)),
        Seq("hB1".U, "b0101_110001".U, "b0101_110001".U, 0.U(1.W)),
        Seq("hB2".U, "b0101_110010".U, "b0101_110010".U, 0.U(1.W)),
        Seq("hB3".U, "b0101_010011".U, "b0101_010011".U, 0.U(1.W)),
        Seq("hB4".U, "b0101_110100".U, "b0101_110100".U, 0.U(1.W)),
        Seq("hB5".U, "b0101_010101".U, "b0101_010101".U, 0.U(1.W)),
        Seq("hB6".U, "b0101_010110".U, "b0101_010110".U, 0.U(1.W)),
        Seq("hB7".U, "b0101_010111".U, "b0101_101000".U, 1.U(1.W)),
        Seq("hB8".U, "b0101_110011".U, "b0101_001100".U, 1.U(1.W)),
        Seq("hB9".U, "b0101_011001".U, "b0101_011001".U, 0.U(1.W)),
        Seq("hBA".U, "b0101_011010".U, "b0101_011010".U, 0.U(1.W)),
        Seq("hBB".U, "b0101_011011".U, "b0101_100100".U, 1.U(1.W)),
        Seq("hBC".U, "b0101_011100".U, "b0101_011100".U, 0.U(1.W)),
        Seq("hBD".U, "b0101_011101".U, "b0101_100010".U, 1.U(1.W)),
        Seq("hBE".U, "b0101_011110".U, "b0101_100001".U, 1.U(1.W)),
        Seq("hBF".U, "b0101_110101".U, "b0101_001010".U, 1.U(1.W)),
        Seq("hC0".U, "b0110_111001".U, "b0110_000110".U, 1.U(1.W)),
        Seq("hC1".U, "b0110_101110".U, "b0110_010001".U, 1.U(1.W)),
        Seq("hC2".U, "b0110_101101".U, "b0110_010010".U, 1.U(1.W)),
        Seq("hC3".U, "b0110_100011".U, "b0110_100011".U, 0.U(1.W)),
        Seq("hC4".U, "b0110_101011".U, "b0110_010100".U, 1.U(1.W)),
        Seq("hC5".U, "b0110_100101".U, "b0110_100101".U, 0.U(1.W)),
        Seq("hC6".U, "b0110_100110".U, "b0110_100110".U, 0.U(1.W)),
        Seq("hC7".U, "b0110_000111".U, "b0110_111000".U, 0.U(1.W)),
        Seq("hC8".U, "b0110_100111".U, "b0110_011000".U, 1.U(1.W)),
        Seq("hC9".U, "b0110_101001".U, "b0110_101001".U, 0.U(1.W)),
        Seq("hCA".U, "b0110_101010".U, "b0110_101010".U, 0.U(1.W)),
        Seq("hCB".U, "b0110_001011".U, "b0110_001011".U, 0.U(1.W)),
        Seq("hCC".U, "b0110_101100".U, "b0110_101100".U, 0.U(1.W)),
        Seq("hCD".U, "b0110_001101".U, "b0110_001101".U, 0.U(1.W)),
        Seq("hCE".U, "b0110_001110".U, "b0110_001110".U, 0.U(1.W)),
        Seq("hCF".U, "b0110_111010".U, "b0110_000101".U, 1.U(1.W)),
        Seq("hD0".U, "b0110_110110".U, "b0110_001001".U, 1.U(1.W)),
        Seq("hD1".U, "b0110_110001".U, "b0110_110001".U, 0.U(1.W)),
        Seq("hD2".U, "b0110_110010".U, "b0110_110010".U, 0.U(1.W)),
        Seq("hD3".U, "b0110_010011".U, "b0110_010011".U, 0.U(1.W)),
        Seq("hD4".U, "b0110_110100".U, "b0110_110100".U, 0.U(1.W)),
        Seq("hD5".U, "b0110_010101".U, "b0110_010101".U, 0.U(1.W)),
        Seq("hD6".U, "b0110_010110".U, "b0110_010110".U, 0.U(1.W)),
        Seq("hD7".U, "b0110_010111".U, "b0110_101000".U, 1.U(1.W)),
        Seq("hD8".U, "b0110_110011".U, "b0110_001100".U, 1.U(1.W)),
        Seq("hD9".U, "b0110_011001".U, "b0110_011001".U, 0.U(1.W)),
        Seq("hDA".U, "b0110_011010".U, "b0110_011010".U, 0.U(1.W)),
        Seq("hDB".U, "b0110_011011".U, "b0110_100100".U, 1.U(1.W)),
        Seq("hDC".U, "b0110_011100".U, "b0110_011100".U, 0.U(1.W)),
        Seq("hDD".U, "b0110_011101".U, "b0110_100010".U, 1.U(1.W)),
        Seq("hDE".U, "b0110_011110".U, "b0110_100001".U, 1.U(1.W)),
        Seq("hDF".U, "b0110_110101".U, "b0110_001010".U, 1.U(1.W)),
        Seq("hE0".U, "b1000_111001".U, "b0111_000110".U, 0.U(1.W)),
        Seq("hE1".U, "b1000_101110".U, "b0111_010001".U, 0.U(1.W)),
        Seq("hE2".U, "b1000_101101".U, "b0111_010010".U, 0.U(1.W)),
        Seq("hE3".U, "b0111_100011".U, "b1000_100011".U, 1.U(1.W)),
        Seq("hE4".U, "b1000_101011".U, "b0111_010100".U, 0.U(1.W)),
        Seq("hE5".U, "b0111_100101".U, "b1000_100101".U, 1.U(1.W)),
        Seq("hE6".U, "b0111_100110".U, "b1000_100110".U, 1.U(1.W)),
        Seq("hE7".U, "b0111_000111".U, "b1000_111000".U, 1.U(1.W)),
        Seq("hE8".U, "b1000_100111".U, "b0111_011000".U, 0.U(1.W)),
        Seq("hE9".U, "b0111_101001".U, "b1000_101001".U, 1.U(1.W)),
        Seq("hEA".U, "b0111_101010".U, "b1000_101010".U, 1.U(1.W)),
        Seq("hEB".U, "b0111_001011".U, "b0001_001011".U, 1.U(1.W)),
        Seq("hEC".U, "b0111_101100".U, "b1000_101100".U, 1.U(1.W)),
        Seq("hED".U, "b0111_001101".U, "b0001_001101".U, 1.U(1.W)),
        Seq("hEE".U, "b0111_001110".U, "b0001_001110".U, 1.U(1.W)),
        Seq("hEF".U, "b1000_111010".U, "b0111_000101".U, 0.U(1.W)),
        Seq("hF0".U, "b1000_110110".U, "b0111_001001".U, 0.U(1.W)),
        Seq("hF1".U, "b1110_110001".U, "b1000_110001".U, 1.U(1.W)),
        Seq("hF2".U, "b1110_110010".U, "b1000_110010".U, 1.U(1.W)),
        Seq("hF3".U, "b0111_010011".U, "b1000_010011".U, 1.U(1.W)),
        Seq("hF4".U, "b1110_110100".U, "b1000_110100".U, 1.U(1.W)),
        Seq("hF5".U, "b0111_010101".U, "b1000_010101".U, 1.U(1.W)),
        Seq("hF6".U, "b0111_010110".U, "b1000_010110".U, 1.U(1.W)),
        Seq("hF7".U, "b1000_010111".U, "b0111_101000".U, 0.U(1.W)),
        Seq("hF8".U, "b1000_110011".U, "b0111_001100".U, 0.U(1.W)),
        Seq("hF9".U, "b0111_011001".U, "b1000_011001".U, 1.U(1.W)),
        Seq("hFA".U, "b0111_011010".U, "b1000_011010".U, 1.U(1.W)),
        Seq("hFB".U, "b1000_011011".U, "b0111_100100".U, 0.U(1.W)),
        Seq("hFC".U, "b0111_011100".U, "b1000_011100".U, 1.U(1.W)),
        Seq("hFD".U, "b1000_011101".U, "b0111_100010".U, 0.U(1.W)),
        Seq("hFE".U, "b1000_011110".U, "b0111_100001".U, 0.U(1.W)),
        Seq("hFF".U, "b1000_110101".U, "b0111_001010".U, 0.U(1.W))
    )

    val K_28_y = Seq(
        Seq("h1C".U, "b0010_111100".U, "b1101_000011".U, 0.U(1.W)), // k28.0
        Seq("h7C".U, "b1100_111100".U, "b0011_000011".U, 1.U(1.W)), // k28.3
        Seq("h9C".U, "b0100_111100".U, "b1011_000011".U, 0.U(1.W)), // k28.4
        Seq("hBC".U, "b0101_111100".U, "b1010_000011".U, 1.U(1.W)), // k28.5
        Seq("hDC".U, "b0110_111100".U, "b1001_000011".U, 1.U(1.W))  // k28.6
    )
}